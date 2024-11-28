Important requirements for .gitignore and .idea:
- The .gitignore file works globally within the repository, so it's not necessary for each collaborator to add their own .DS_Store exclusion manually.
- Similarly, any .idea files you decide to ignore will also apply to everyone, regardless of the OS.
- Make sure everyone has configured their Git correctly for cross-platform work, particularly regarding line endings (e.g., CRLF vs LF). Including this in your .gitattributes file helps

Note on emission calculations: 
- An assumption was made for automobile emission calculation; the rate of KG/KM of CO2E for when the user is unsure of their type of car is estimated to be 0.2 KG/KM 