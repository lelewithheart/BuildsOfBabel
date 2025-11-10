This project includes a lightweight Maven wrapper script (mvnw.cmd / mvnw)
that downloads a chosen Apache Maven binary to .mvn/wrapper/apache-maven-<version>
on first run and then invokes it.

Notes:
- The wrapper is a convenience so you don't need to install Maven system-wide.
- On Windows the wrapper uses PowerShell to download and Expand-Archive.
- On *nix the wrapper uses curl and unzip; make sure those are available.
- If you prefer the official Maven Wrapper (maven-wrapper.jar), you can replace
  these scripts with the official distribution. This lightweight script is
  sufficient to build this repository without system Maven installed.
