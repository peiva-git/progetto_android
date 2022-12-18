# University project - My Trip Planner
[![Build Status](https://app.travis-ci.com/peiva-git/progetto_android.svg?branch=master)](https://app.travis-ci.com/peiva-git/progetto_android)

Exam project for the Android course at the University of Trieste. 
Built with Android Studio, using Gradle, Java, Firebase and Travis CI. 

## Requirements
The application needs at least API level 21 to run (Android 5.0 Lollipop)

## Description
This application allows authenticated users to store and share trips through Firebase. 
Each trip can have multiple images and some basic information attached, such as a destination, duration and a brief description.

## Testing
The fragments which require the user to input data also provide some error messages in case of incorrect values.
Input validation is tested using the Espresso framework.

## Possible future developments
New features that could be added in the future include:
- Fragment displaying user data
- Ability to delete/modify created users
- Ability to modify added trips
