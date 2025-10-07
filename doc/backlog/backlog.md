# Product Backlog — Event Lottery System (Part 2)

> Story Points scale: 1 = tiny, 3 = small, 5 = medium, 8 = large, 13 = very large  
> Risk: L = Low  M = Medium  H = High  
> Release: Half = Half-way checkpoint  Post = Later iteration

| ID | User Story | Acceptance Criteria (summary) | SP | Risk | Release |
|----|-------------|-------------------------------|----|------|----------|
| US 01.01.03 | Entrant views list of joinable events | Given home page → When refresh → Then list of events displayed | 3 | L | Half |
| US 01.06.01 | Scan QR to view event details | Given camera access → When QR scanned → Then event detail screen opens | 5 | M | Half |
| US 01.06.02 | Join event from detail page | Given detail view → When tap “Join” → Then added to waiting list | 3 | L | Half |
| US 01.01.02 | Leave waiting list | Given joined → When tap “Leave” → Then removed and count decreases | 2 | L | Half |
| US 02.01.01 | Organizer creates event & QR | Given create form → When publish → Then event saved + QR generated | 5 | M | Half |
| US 02.04.01 | Organizer uploads event poster | Given details page → When upload → Then image stored & displayed | 3 | L | Half |
| US 02.02.01 | Organizer views waiting list | Given event → When open “Waitlist” → Then list of entrants shown | 3 | L | Half |
| US 02.05.02 | Organizer samples N attendees | Given deadline → When draw → Then N entrants selected | 8 | H | Post |
| US 01.04.01 | Entrant receives winning notification | Given selected → When draw complete → Then receives in-app notice | 5 | M | Post |
| US 02.06.03 | Organizer views final attendee list | Given confirmations → When open “Final list” → Then attendees shown | 3 | L | Post |

### Traceability
Each story links to corresponding mockups, storyboards, and CRC classes.
