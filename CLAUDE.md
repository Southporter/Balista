# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Ballista is a minimalist Android launcher application built with Jetpack Compose and Material Design 3. It serves as a home screen replacement, providing a clean text-based interface for launching common Android apps.

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Clean build
./gradlew clean

# Run tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

## Architecture

### Core Components
- **MainActivity.kt**: Main launcher screen with LazyColumn of apps
- **SettingsActivity.kt**: Configuration screen for toggling apps and accessing system settings
- **AppRepository.kt**: Singleton data management layer using SharedPreferences and StateFlow
- **AppItem.kt**: Data class representing launchable applications

### UI Architecture
- Built with Jetpack Compose and Material Design 3
- Uses dynamic theming (Android 12+) with fallback colors
- Single-task launch mode with edge-to-edge display
- Implements repository pattern for reactive state management

### Key Features
- Acts as both LAUNCHER and HOME category intent handler
- Supports 15 pre-configured common Android apps
- Direct component launching with fallback mechanisms
- Scroll indicators for pagination when more than 9 apps are visible
- Long press access to settings

## Configuration

- **Package**: `com.github.southporter.ballista`
- **Target SDK**: 35 (Android 15)
- **Min SDK**: 29 (Android 10)
- **Kotlin**: 2.0.21
- **Compose BOM**: 2024.09.00

## App Management

The launcher manages apps through `AppRepository`:
- Default apps are defined in `getDefaultApps()`
- App visibility controlled via SharedPreferences
- Component resolution handles direct app launching
- Fallback to main launcher if specific app unavailable

## UI Layout Guidelines

- Main screen uses centered vertical layout with 48dp horizontal padding
- Apps display optimal at 9 items per screen with 80dp top padding
- Scroll indicators appear on right side when more than 9 apps present
- Long press on any app opens settings screen