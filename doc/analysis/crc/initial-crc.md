# CRC Cards (Initial)

## Event
**Responsibilities:** store name, description, location, time, registration window, capacity, poster URL, organizer ID  
**Collaborators:** Organizer, Waitlist, PosterStorage, Repository

## Entrant
**Responsibilities:** hold profile/device ID, join/leave waitlist, accept/decline invitations  
**Collaborators:** Waitlist, NotificationService, Repository

## Organizer
**Responsibilities:** create/edit/publish events, view waitlists, trigger lottery draw  
**Collaborators:** Event, LotteryManager, PosterStorage, Repository

## Waitlist
**Responsibilities:** manage entrants for one event (add/remove/count/query)  
**Collaborators:** Event, Entrant, Repository, LotteryManager

## LotteryManager
**Responsibilities:** randomly select N entrants, handle replacements  
**Collaborators:** Waitlist, NotificationService, Repository

## PosterStorage
**Responsibilities:** upload/update/fetch poster images (Firebase Storage)  
**Collaborators:** Event, Organizer

## NotificationService
**Responsibilities:** send win/loss/group notifications, log messages  
**Collaborators:** Entrant, Organizer, LotteryManager

## Repository (FirebaseService)
**Responsibilities:** Firestore CRUD (events, entrants, waitlists, notifications)  
**Collaborators:** Event, Entrant, Waitlist, LotteryManager
