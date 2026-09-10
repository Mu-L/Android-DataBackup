use std::error::Error;
use std::fs;
use std::path::Path;
use std::sync::{Arc, Mutex};
use std::time::{SystemTime, UNIX_EPOCH};

use rustic::RusticProgressCallback;

fn temp_path(name: &str) -> Result<std::path::PathBuf, Box<dyn Error>> {
    Ok(std::env::temp_dir().join(format!(
        "rustic-{name}-{}",
        SystemTime::now().duration_since(UNIX_EPOCH)?.as_nanos()
    )))
}

#[test]
fn detects_and_validates_repository() -> Result<(), Box<dyn Error>> {
    let root = temp_path("detect-repository")?;
    let repository = root.join("repo");
    let repository_path = repository.to_str().unwrap();
    let password = "password";

    assert!(!rustic::repository_exists(repository_path)?);
    fs::create_dir_all(&repository)?;
    fs::write(repository.join("unrelated"), b"data")?;
    assert!(!rustic::repository_exists(repository_path)?);

    fs::remove_dir_all(&repository)?;
    rustic::init_repository(repository_path, password)?;
    assert!(rustic::repository_exists(repository_path)?);
    rustic::validate_repository(repository_path, password)?;
    assert!(rustic::validate_repository(repository_path, "incorrect").is_err());

    fs::remove_dir_all(root)?;
    Ok(())
}

#[test]
fn create_restore_and_check_snapshot_lifecycle() -> Result<(), Box<dyn Error>> {
    run_snapshot_lifecycle(
        "snapshot-lifecycle",
        "note.txt",
        b"Hello from rustic",
        |repository, password, source_paths, tags| {
            rustic::create_snapshot(repository.to_str().unwrap(), password, source_paths, tags)
        },
    )
}

#[derive(Debug)]
struct RecordingProgress {
    events: Arc<Mutex<Vec<(u64, u64, f32)>>>,
}

impl RusticProgressCallback for RecordingProgress {
    fn on_progress(&self, bytes_done: u64, speed: u64, progress: f32) {
        println!("progress: bytes_done={bytes_done}, speed={speed}, progress={progress}");
        self.events
            .lock()
            .unwrap()
            .push((bytes_done, speed, progress));
    }
}

#[test]
fn create_restore_and_check_snapshot_lifecycle_with_progress() -> Result<(), Box<dyn Error>> {
    let content = vec![b'x'; 1024 * 1024];
    let events = Arc::new(Mutex::new(Vec::new()));

    run_snapshot_lifecycle(
        "snapshot-lifecycle-progress",
        "payload.bin",
        &content,
        |repository, password, source_paths, tags| {
            rustic::create_snapshot_with_progress(
                repository.to_str().unwrap(),
                password,
                source_paths,
                tags,
                RecordingProgress {
                    events: events.clone(),
                },
            )
        },
    )?;

    let events = events.lock().unwrap();
    assert!(!events.is_empty());
    assert!(
        events
            .iter()
            .all(|(bytes_done, _speed, progress)| *bytes_done > 0
                && *progress >= 0.0
                && *progress <= 1.0)
    );
    assert!(events.windows(2).all(|window| window[0].0 <= window[1].0));
    println!("progress events: {}", events.len());

    Ok(())
}

#[test]
fn create_and_restore_snapshot_with_multiple_direct_sources() -> Result<(), Box<dyn Error>> {
    let root = temp_path("multi-source-snapshot")?;
    let repository = root.join("repo");
    let app = root.join("app");
    let files = root.join("files");
    let staging = root.join("staging");
    let restore = root.join("restore");
    let password = "password";

    fs::create_dir_all(&app)?;
    fs::create_dir_all(&files)?;
    fs::create_dir_all(&staging)?;
    fs::write(app.join("app-data.txt"), b"app")?;
    fs::write(files.join("user-file.txt"), b"file")?;
    fs::write(staging.join("manifest.json"), b"manifest")?;

    rustic::init_repository(repository.to_str().unwrap(), password)?;
    let source_paths = [app, files, staging].map(|path| path.to_string_lossy().into_owned());
    let snapshot_id = rustic::create_snapshot(
        repository.to_str().unwrap(),
        password,
        &source_paths,
        &["databackup".to_string()],
    )?;

    assert!(!snapshot_id.is_empty());
    rustic::restore_snapshot(
        repository.to_str().unwrap(),
        password,
        &snapshot_id,
        restore.to_str().unwrap(),
    )?;
    rustic::check_repository(repository.to_str().unwrap(), password)?;
    assert_eq!(fs::read(find_file(&restore, "app-data.txt")?)?, b"app");
    assert_eq!(fs::read(find_file(&restore, "user-file.txt")?)?, b"file");
    assert_eq!(
        fs::read(find_file(&restore, "manifest.json")?)?,
        b"manifest"
    );

    fs::remove_dir_all(root)?;
    Ok(())
}

#[test]
fn lists_all_snapshots_with_complete_metadata() -> Result<(), Box<dyn Error>> {
    let root = temp_path("list-config-snapshots")?;
    let repository = root.join("repo");
    let source = root.join("source");
    let password = "password";
    let first_tag = "databackup:config:first";
    let second_tag = "databackup:config:second";

    fs::create_dir_all(&source)?;
    fs::write(source.join("data.txt"), b"first")?;
    rustic::init_repository(repository.to_str().unwrap(), password)?;
    assert_eq!(rustic::list_snapshots(repository.to_str().unwrap(), password)?, "[]");
    assert!(rustic::list_snapshots(repository.to_str().unwrap(), "incorrect-password").is_err());
    let source_paths = [source.to_string_lossy().into_owned()];
    let first_snapshot = rustic::create_snapshot(
        repository.to_str().unwrap(),
        password,
        &source_paths,
        &["databackup".to_string(), first_tag.to_string()],
    )?;

    fs::write(source.join("data.txt"), b"second")?;
    let second_snapshot = rustic::create_snapshot(
        repository.to_str().unwrap(),
        password,
        &source_paths,
        &["databackup".to_string(), second_tag.to_string()],
    )?;

    let listed_snapshots = rustic::list_snapshots(repository.to_str().unwrap(), password)?;
    let snapshots: serde_json::Value = serde_json::from_str(&listed_snapshots)?;
    let snapshots = snapshots.as_array().unwrap();
    assert_eq!(snapshots.len(), 2);
    let listed_ids = snapshots
        .iter()
        .map(|snapshot| snapshot["id"].as_str().unwrap())
        .collect::<Vec<_>>();
    assert!(listed_ids.iter().any(|id| id.starts_with(&first_snapshot)));
    assert!(listed_ids.iter().any(|id| id.starts_with(&second_snapshot)));
    assert!(snapshots[0]["created_at"].is_i64() || snapshots[0]["created_at"].is_u64());
    assert!(snapshots[0]["created_at"].as_i64().unwrap() >= snapshots[1]["created_at"].as_i64().unwrap());
    assert!(snapshots[0]["time"].is_string());
    assert!(snapshots[0]["paths"].is_array());
    assert!(snapshots[0]["tags"].is_array());
    assert!(snapshots[0]["summary"].is_object());

    fs::remove_dir_all(root)?;
    Ok(())
}

fn run_snapshot_lifecycle(
    temp_name: &str,
    file_name: &str,
    content: &[u8],
    create_snapshot: impl FnOnce(&Path, &str, &[String], &[String]) -> Result<String, Box<dyn Error>>,
) -> Result<(), Box<dyn Error>> {
    let root = temp_path(temp_name)?;
    let repository = root.join("repo");
    let source = root.join("source");
    let restore = root.join("restore");
    let password = "password";
    let source_paths = [source.to_string_lossy().into_owned()];
    let tags = ["instrumented".to_string()];

    fs::create_dir_all(source.join("nested"))?;
    fs::write(source.join("nested").join(file_name), content)?;

    rustic::init_repository(repository.to_str().unwrap(), password)?;
    let snapshot_id = create_snapshot(&repository, password, &source_paths, &tags)?;

    assert!(!snapshot_id.is_empty());

    rustic::restore_snapshot(
        repository.to_str().unwrap(),
        password,
        &snapshot_id,
        restore.to_str().unwrap(),
    )?;
    rustic::check_repository(repository.to_str().unwrap(), password)?;

    let restored = find_file(&restore, file_name)?;
    assert_eq!(fs::read(restored)?, content);

    fs::remove_dir_all(root)?;
    Ok(())
}

fn find_file(root: &Path, name: &str) -> Result<std::path::PathBuf, Box<dyn Error>> {
    for entry in fs::read_dir(root)? {
        let path = entry?.path();
        if path.is_dir() {
            if let Ok(found) = find_file(&path, name) {
                return Ok(found);
            }
        } else if path.file_name().is_some_and(|file_name| file_name == name) {
            return Ok(path);
        }
    }

    Err(format!("missing restored file {name}").into())
}

#[test]
fn reads_metadata_from_snapshot_without_restoring_live_files() -> Result<(), Box<dyn Error>> {
    let root = temp_path("read-snapshot-metadata")?;
    let repository = root.join("repo");
    let source = root.join("source");
    fs::create_dir_all(&source)?;
    let metadata = source.join("manifest.json");
    fs::write(&metadata, r#"{"schemaVersion":1}"#)?;
    rustic::init_repository(repository.to_str().unwrap(), "password")?;
    let snapshot = rustic::create_snapshot(
        repository.to_str().unwrap(), "password", &[source.to_string_lossy().into_owned()], &[],
    )?;
    fs::write(&metadata, "changed on device")?;
    let paths = vec![metadata.to_string_lossy().into_owned()];
    let files: serde_json::Value = serde_json::from_str(&rustic::read_snapshot_text_files(
        repository.to_str().unwrap(), "password", &snapshot, &paths,
    )?)?;
    assert_eq!(files[&paths[0]], r#"{"schemaVersion":1}"#);
    assert_eq!(fs::read_to_string(&metadata)?, "changed on device");
    assert!(rustic::read_snapshot_text_files(repository.to_str().unwrap(), "password", "latest", &paths).is_err());
    assert!(rustic::read_snapshot_text_files(
        repository.to_str().unwrap(), "password", &snapshot, &[source.join("missing.json").to_string_lossy().into_owned()],
    ).is_err());
    fs::remove_dir_all(root)?;
    Ok(())
}

#[test]
fn deletes_only_selected_snapshot_and_preserves_shared_data() -> Result<(), Box<dyn Error>> {
    let root = temp_path("delete-selected-snapshot")?;
    let repository = root.join("repo");
    let source = root.join("source");
    let restore = root.join("restore");
    let password = "password";
    fs::create_dir_all(&source)?;
    fs::write(source.join("shared.txt"), b"shared data")?;
    let repository_path = repository.to_str().unwrap();
    rustic::init_repository(repository_path, password)?;
    let paths = [source.to_string_lossy().into_owned()];
    rustic::create_snapshot(repository_path, password, &paths, &["first".into()])?;
    rustic::create_snapshot(repository_path, password, &paths, &["second".into()])?;
    let snapshots: serde_json::Value = serde_json::from_str(&rustic::list_snapshots(repository_path, password)?)?;
    let selected = snapshots[0]["id"].as_str().unwrap();
    let remaining = snapshots[1]["id"].as_str().unwrap();
    for invalid in ["", "latest", &selected[..8], &"z".repeat(64), &"0".repeat(64)] {
        assert!(rustic::delete_snapshot(repository_path, password, invalid).is_err());
    }
    assert!(rustic::delete_snapshot(repository_path, "wrong-password", selected).is_err());
    let unchanged: serde_json::Value = serde_json::from_str(&rustic::list_snapshots(repository_path, password)?)?;
    assert_eq!(unchanged.as_array().unwrap().len(), 2);
    let after_delete: serde_json::Value = serde_json::from_str(&rustic::delete_snapshot(repository_path, password, selected)?)?;
    assert_eq!(after_delete.as_array().unwrap().len(), 1);
    assert_eq!(after_delete[0], snapshots[1]);
    assert!(rustic::delete_snapshot(repository_path, password, selected).is_err());
    let listed: serde_json::Value = serde_json::from_str(&rustic::list_snapshots(repository_path, password)?)?;
    assert_eq!(listed, after_delete);
    assert_eq!(listed.as_array().unwrap().len(), 1);
    assert_eq!(listed[0]["id"].as_str().unwrap(), remaining);
    rustic::restore_snapshot(repository_path, password, remaining, restore.to_str().unwrap())?;
    let restored_file = restore.join(source.strip_prefix(Path::new("/"))?).join("shared.txt");
    assert_eq!(fs::read(restored_file)?, b"shared data");
    rustic::check_repository(repository_path, password)?;
    assert_eq!(rustic::delete_snapshot(repository_path, password, remaining)?, "[]");
    assert_eq!(rustic::list_snapshots(repository_path, password)?, "[]");
    fs::remove_dir_all(root)?;
    Ok(())
}
