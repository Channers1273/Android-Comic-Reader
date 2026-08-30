Features
- New design on the reading list page
- An edit reading list option (menu bar with options: rename, reorder, delete -> Brings up a pop-up "are you sure? This won't delete the comics in this list off your device") and an option just to pick up where you left off and start reading
- A central color scheme
- Recently read (way to jump into indiv comic, or reading list)
- Zoom on a page

Issues
- The panels in the reader are ugly. Want a better implementation
- There is no way to delete comics.
- pageCount from the scan can be off by one if a .cbz contains a non-image entry like ComicInfo.xml, since readMetadata counts all non-directory zip entries. That's a separate pre-existing issue, not something this change touches
- Weird blank page at the end of a comic
- Is the wasted vertical space on the library TopAppBar fixable?\

Thoughts:
- If there comes a time where I decide there is no settings page, do I use tabbed navigation instead?
- What is the name of the app?
- How do I make an icon for the app itself? Have to take into account how themes on system can retheme it?

