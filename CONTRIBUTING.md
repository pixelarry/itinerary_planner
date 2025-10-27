# Contributing to Itinerary Planner

Thank you for your interest in contributing to Itinerary Planner! This document provides guidelines and information for contributors.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Project Structure](#project-structure)
- [Coding Standards](#coding-standards)
- [Commit Guidelines](#commit-guidelines)
- [Pull Request Process](#pull-request-process)
- [Issue Guidelines](#issue-guidelines)
- [Testing](#testing)
- [Documentation](#documentation)
- [Release Process](#release-process)

## Code of Conduct

This project adheres to a code of conduct that we expect all contributors to follow:

- Be respectful and inclusive
- Welcome newcomers and help them learn
- Focus on constructive feedback
- Respect different viewpoints and experiences
- Show empathy towards other community members

## Getting Started

### Prerequisites

Before contributing, ensure you have:

- **Android Studio** (latest stable version)
- **JDK 8** or higher
- **Git** for version control
- **Android SDK** with API level 24+ (minimum supported)
- Basic knowledge of **Kotlin** and **Android development**

### Fork and Clone

1. Fork the repository on GitHub
2. Clone your fork locally:
   ```bash
   git clone https://github.com/yourusername/itinerary_planner.git
   cd itinerary_planner
   ```
3. Add the upstream repository:
   ```bash
   git remote add upstream https://github.com/pixelarry/itinerary_planner.git
   ```

## Development Setup

### 1. Open in Android Studio

1. Launch Android Studio
2. Select "Open an existing project"
3. Navigate to your cloned repository folder
4. Wait for Gradle sync to complete

### 2. Build the Project

```bash
./gradlew assembleDebug
```

### 3. Run Tests

```bash
./gradlew test
```

### 4. Create a Branch

Create a new branch for your feature or bugfix:

```bash
git checkout -b feature/your-feature-name
# or
git checkout -b bugfix/issue-number-description
```

## Project Structure

```
app/src/main/java/com/pixelarry/itinerary_planner/
├── data/                    # Data layer
│   ├── PlansDatabase.kt    # SQLite database helper
│   └── PlansRepository.kt  # Data repository
├── ui/                     # UI components
│   ├── ItineraryAdapter.kt # RecyclerView adapters
│   ├── PlanModels.kt       # Data models
│   └── PlansAdapter.kt
├── MainActivity.kt         # Main activity
├── NewPlanActivity.kt      # Plan creation
├── PlanDetailsActivity.kt  # Plan details view
└── OpenSourceLibrariesActivity.kt # About libraries
```

### Key Technologies

- **Language**: Kotlin
- **UI Framework**: Android Views (XML layouts)
- **Database**: SQLite
- **Image Loading**: Glide
- **JSON Parsing**: Gson
- **Image Cropping**: UCrop
- **Build System**: Gradle with Kotlin DSL

## Coding Standards

### Kotlin Style Guide

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Prefer `val` over `var` when possible
- Use extension functions for utility methods
- Follow camelCase for variables and functions
- Use PascalCase for classes and interfaces

### Android Best Practices

- Follow [Android Developer Guidelines](https://developer.android.com/guide)
- Use ViewBinding or DataBinding for UI
- Implement proper lifecycle management
- Handle configuration changes appropriately
- Use appropriate Android architecture components
- Follow Material Design principles

### Code Formatting

- Use 4 spaces for indentation
- Maximum line length: 120 characters
- Use trailing commas in multi-line declarations
- Group imports logically (Android, third-party, local)

### Example Code Style

```kotlin
class ExampleActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityExampleBinding
    private val repository by lazy { PlansRepository(this) }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExampleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        observeData()
    }
    
    private fun setupUI() {
        // UI setup code
    }
    
    private fun observeData() {
        // Data observation code
    }
}
```

## Commit Guidelines

### Commit Message Format

Use the following format for commit messages:

```
type(scope): brief description

Detailed explanation of the change, if necessary.

Fixes #issue-number
```

### Types

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

### Examples

```
feat(ui): add dark mode toggle to settings

Implements a toggle switch in the settings activity that allows
users to switch between light and dark themes. The preference
is persisted using SharedPreferences.

Fixes #123
```

```
fix(database): resolve crash when deleting plans

Fixed NullPointerException that occurred when trying to delete
a plan that was already removed from the database.

Fixes #456
```

## Pull Request Process

### Before Submitting

1. **Test your changes thoroughly**
   - Run the app on different screen sizes
   - Test on different Android versions (API 24+)
   - Verify no crashes or memory leaks

2. **Update documentation** if needed
   - Update README.md for new features
   - Add code comments for complex logic
   - Update this CONTRIBUTING.md if needed

3. **Check code quality**
   - Run lint checks: `./gradlew lint`
   - Ensure all tests pass: `./gradlew test`
   - Verify no build warnings

### Pull Request Template

When creating a PR, include:

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Tested on multiple devices
- [ ] All existing tests pass
- [ ] Added new tests for new functionality

## Screenshots (if applicable)
Add screenshots for UI changes

## Checklist
- [ ] Code follows project style guidelines
- [ ] Self-review completed
- [ ] Documentation updated
- [ ] No build warnings
```

### Review Process

1. **Automated Checks**: CI/CD pipeline runs tests and linting
2. **Code Review**: At least one maintainer reviews the code
3. **Testing**: Reviewer tests the changes
4. **Approval**: Maintainer approves and merges

## Issue Guidelines

### Bug Reports

When reporting bugs, include:

- **Clear title** describing the issue
- **Steps to reproduce** the problem
- **Expected behavior** vs **actual behavior**
- **Device information**:
  - Android version
  - Device model
  - App version
- **Screenshots** or **screen recordings** if applicable
- **Logcat output** for crashes

### Feature Requests

For feature requests, include:

- **Clear description** of the feature
- **Use case** and **benefit** to users
- **Proposed implementation** (if you have ideas)
- **Screenshots** or **mockups** if applicable

### Issue Labels

We use the following labels:

- `bug`: Something isn't working
- `enhancement`: New feature or request
- `documentation`: Improvements to documentation
- `good first issue`: Good for newcomers
- `help wanted`: Extra attention needed
- `priority: high`: High priority issues
- `priority: low`: Low priority issues

## Testing

### Unit Tests

- Write unit tests for business logic
- Test data models and utility functions
- Aim for high code coverage

### UI Tests

- Test critical user flows
- Verify UI components work correctly
- Test on different screen sizes

### Manual Testing

- Test on physical devices when possible
- Test on different Android versions
- Verify accessibility features work

### Running Tests

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "com.pixelarry.itinerary_planner.ExampleTest"

# Run with coverage
./gradlew testDebugUnitTestCoverage
```

## Documentation

### Code Documentation

- Add KDoc comments for public APIs
- Document complex algorithms or business logic
- Include examples in documentation

### README Updates

- Update README.md for new features
- Keep installation instructions current
- Update screenshots when UI changes

### API Documentation

- Document any new public APIs
- Include parameter descriptions
- Provide usage examples

## Release Process

### Version Numbering

We follow [Semantic Versioning](https://semver.org/):

- **MAJOR**: Breaking changes
- **MINOR**: New features (backward compatible)
- **PATCH**: Bug fixes (backward compatible)

### Release Checklist

1. Update version numbers in `build.gradle.kts`
2. Update CHANGELOG.md
3. Create release notes
4. Tag the release
5. Build and test release APK
6. Publish to GitHub Releases

## Getting Help

### Communication Channels

- **GitHub Issues**: Bug reports and feature requests
- **GitHub Discussions**: General questions and ideas
- **Contact Form**: [Direct contact](https://forms.gle/qKYahftqpdnrLhZ49)

### Resources

- [Android Developer Documentation](https://developer.android.com/)
- [Kotlin Documentation](https://kotlinlang.org/docs/)
- [Material Design Guidelines](https://material.io/design)
- [SQLite Database Guide](https://developer.android.com/training/data-storage/sqlite)

## Recognition

Contributors will be recognized in:

- README.md contributors section
- Release notes for significant contributions
- GitHub contributors page

## License

By contributing to Itinerary Planner, you agree that your contributions will be licensed under the GNU General Public License v3.0.

---

Thank you for contributing to Itinerary Planner! Your efforts help make this project better for everyone. 🚀
