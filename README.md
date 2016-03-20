![Flan](/images/logo.png)
> WalkAbout (pronounced: [wôkəˌbout]), 
> an informal stroll among a crowd conducted by an important visitor.

Location Based Content Sharing Platform

### Description
Flâneurs is an app where users can create and consume content that is tied to a real world coordinate.  The caveat being, that users can only view this content after they have visited the physical location where that content was created.

We are interested in capturing and sharing the story of a city, the thoughts and experiences of those who live in the same geographic location, but may not otherwise cross paths.  

### Required User Stories

#### General
- [x] Pick up Flâneurs and view them in your inbox

#### Auth Screen
- [x] Allow user to OAuth with Facebook

#### Map / Feed Screens
- [x] Allow user to see different Flâneurs on a map view
- [x] Allow users to see Flâneurs posted by friends and all app users in a feed
 
#### Compose Screen
- [x] Create Flâneurs, either a photo or video with a caption, and drop it at your current location
 
#### Inbox Screen
- [x] Allow users to see their picked up Flâneurs in an inbox
 
#### Detail Screen
- [x] Allow user to view the content associated with their Flâneurs
- [x] Allow users to comment on picked up Flâneurs
- [x] Allow users to upvote/downvote their picked up Flâneurs 

### Optional User Stories
- [x] Receieve notifications about flaneur immediately
- [x] Badge inbox with number of new pickups
- [x] Ability to delete flaneurs in inbox
- [x] Ability to view user profiles and see how many drops/picks up user has done, as well as view their timeline
- [x] Reveal post in inbox, hide post on timeline (Locked + unlocked visual)

### Polish Items
- [x] After viewing inbox item, move it to bottom of list
- [x] Only allow 1 upvote, visually represent button change
- [x] Remove marker from map on pickup
- [ ] Optimize parse caching and fetches
- [ ] Delete old posts
- [ ] Get status bar color back on detail view
- [ ] Have screen scroll up when composing comment
- [ ] Redo search in area on map
- [ ] Endless scroll on timeline
- [ ] Ability to archive flaneurs in inbox
- [ ] Cache parse queries (pinAllInBackground)
- [ ] Kill parseProxy


### Crazy Stretch
- [ ] Move Pickup Service into actual service

### Sprint 3 Demo
![Flan](/images/flan_demo_2.gif)

### Sprint 2 Demo
![Flan](/images/flan_demo_1.gif)
- Facebook auth
- Parse backend
- Maps
- Camera

### Wire Frames
| Screen  | Name | Archetype | Description |
| ------------- | :---: | :---: | :---  | :---  |
| ![auth](/wireframes/wireframe_auth.jpg) | Auth Screen | Login | Authenticate with OAuth 2.0 using Facebook to start using Flaneurs |
| ![map](/wireframes/wireframe_map.jpg) | Map Screen | Map | View nearby Flans in a map |
| ![stream](/wireframes/wireframe_feed.jpg) | Stream | Stream | View Flans in a list prioritized with the ability to filter by location, content and popularity  |
| ![compose](/wireframes/wireframe_compose.jpg) | Compose | Creation | Create a Flan by uploading a photo or video with a caption and the users current location  |
| ![inbox](/wireframes/wireframe_inbox.jpg) | Inbox | Stream | Consume picked up Flans |
| ![detail](/wireframes/wireframe_detail.jpg) | Detail | Detail | View a picked up Flans and vote/comment on it |
| ![profile](/wireframes/wireframe_profile.jpg) | Profile | Profile | View users stats such as dropped/picked up Flans and their activity timeline |

### State Machine
![transitions](/images/Transitions.png)


