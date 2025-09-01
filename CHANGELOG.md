## [v2.0] - 2025-09-01

### Added
- JWT authentication for secure login and protected endpoints
- `/api/v2/auth/login` endpoint for token generation
- `data.sql` for demo users and tasks
- Localisation support with dynamic language switching

### Changed
- Case controller now filters tasks by authenticated user
- Unit tests updated to include auth scenarios

### Fixed
- Improved error handling for unauthorised access

### Undocumented
- Added `Dockerfile` for containerised deployment
- Configured GitHub Actions for:
    - SonarQube code quality scan
    - Docker image build and push
    - Docker Scout vulnerability scan
    - Docker Scout vulnerability scan with results uploaded to GitHub Actions
- Kubernetes manifests for scalable deployment

### Removed
- Public access to task endpoints (now protected by JWT)
