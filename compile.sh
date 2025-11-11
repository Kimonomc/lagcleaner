#!/bin/bash

# Create output directories
mkdir -p bin
mkdir -p target

# Compile Java files
javac -d bin -cp "lib/*" src/main/java/com/kim/lagcleaner/*.java

# Create manifest file
echo "Manifest-Version: 1.0" > MANIFEST.MF
echo "Main-Class: com.kim.lagcleaner.LagCleaner" >> MANIFEST.MF
echo "Class-Path: lib/paper-api-1.21.8-R0.1-SNAPSHOT.jar" >> MANIFEST.MF

# Create JAR file
jar cvfm target/LagCleaner-1.0-SNAPSHOT.jar MANIFEST.MF -C bin . -C src/main/resources .

# Clean up
rm MANIFEST.MF

echo "Build completed! JAR file is in target directory."
