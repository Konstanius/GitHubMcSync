# Custom Project: GitMcSync

## Features

- Manual and fully automatic import / export of files to and from the GitHub repository
- Listening for changes on the repository
- One click commit pushing and installation via client IDE

## Basic usage

### Main principles
- Easier collaboration of multiple developers
- Automatic archiving and code changelog generation
- Faster testing of config changes via One-Click configuration pushes

### Getting started

**Requirements:**
- Developer access on the Minecraft server
- Collaborator access on this GitHub repository
- `IntelliJ IDEA <Community / Ultimate>` ([Link](https://www.jetbrains.com/idea/download/download-thanks.html?platform=windows&code=IIC))
- Git installed on your local computer ([Link](https://github.com/git-for-windows/git/releases/download/v2.35.0.windows.1/Git-2.35.0-64-bit.exe))

**Setting up your environment:**
- Open IntelliJ IDEA
- If a current project is still opened, click on File -> Close Project
- Click "Get from VCS" in the top right
- Select GitHub on the left, log-in with your account if applicable
- Select `server repository` from the list of repositories
- Click Clone

With this you have successfully cloned the repository on your local machine.

**Configuring your environment:** (Not required, but heavily recommended)
- Open settings of the IDE (CTRL + ALT + S)
- Version Control -> Git -> Uncheck "Show Push dialog for Commit and Push"
- Version Control -> Commit -> Check "Use non-modeal commit interface", if not already checked
- Apply and close settings
- Click the "Commit" tab in your IDE (usually on the left border of the window)
- Right-click "Unversioned files" -> add to .gitignore -> .idea/.gitignore
- Make an exemplary change to any file and click the Commit and Push button

### QuickSync file testing
- **Useful for:** Small, chained changes of a configuration of a single plugin such as MythicMobs
- An automated action will be taken as soon as the commit is registered by the Minecraft server
- This action will merge the files of the modified directory with the server files
- **Usage:**
  - Change any configuration files of a plugin
  - Modify the commit message to start with: `/gitupgrade <plugin> <flag> / <Commit changelog message>`
  - Flags are important:
    - If the plugin has a dedicated reload function of the format: `/<plugin> reload` use no flag
    - To reload a plugin using BileTools, use `-b` as flag
    - To restart the server after merging files, use `-r` as flag
    - To do nothing after merging files, use `-s` as flag

## Commands

### `/gitexport <directory>`
Uploads a directory and its contents / subdirectories to the repository.  
Only files with a whitelisted file extension or no file extension will be uploaded.

### `/gitpull <-ai> <-s/-r>`
Creates a manual pull request from the GitHub branch to the server branch and displays it as such.  
Flags:
- -ai - Automatically uses `/gitmerge <rest flags>` to merge the files
- -s - (Only usable with flag -ai) Do not display plugin reload list after installation
- -r - (Only usable with flag -ai) Restart the server afetr installation to apply all changes

### `/gitmerge <token> <-s/-r>`
Pull all files from the GitHub branch to the server branch.  
This command can be automatically executed by clicking the installation message in a detected commit or with the `-ai` flag in the `/gitpull -ai` command.  
After completion it will display a list of all installed plugins which can be individually clicked to reload them with BileTools.  
Flags:
- -s - Do not display plugin reload list after installation
- -r - Restart the server after installation to apply all changes

### `/gitupgrade <plugin> <-s/-r/-b>`
Pull a specific plugin's directory files from the GitHub branch to the server branch.  
Flags:
- -s - Do nothing after installation
- -r - Restart the server afetr installation to apply all changes
- -b - Reload the plugin using BileTools
- none - Use the plugin internal reload command (`/plugin reload`) if it exists

### `/gitmute`
Toggles the visibility of push detects in chat.  
Is not persistent through a reload / restart of the plugin.
