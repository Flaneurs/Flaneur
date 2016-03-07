![Flan](/images/logo.png)
> Flâneur (pronounced: [flɑnœʁ]), 
> from the French noun flâneur, means "stroller", "lounger", "saunterer", or "loafer".
> Flânerie refers to the act of strolling, with all of its accompanying associations.

Location Based Content Sharing Platform

### Description
Flâneurs is an app where users can create and consume content that is tied to a real world coordinate.  The caveat being, that users can only view this content after they have visited the physical location where that content was created.

We are interested in capturing and sharing the story of a city, the thoughts and experiences of those who live in the same geographic location, but may not otherwise cross paths.  

### Required User Stories

#### General
- [ ] Passively pick up Flâneurs and view them in your inbox

#### Auth Screen
- [x] Allow user to OAuth with Facebook

#### Map / Feed Screens
- [x] Allow user to see different Flâneurs on a map view
- [x] Allow users to see Flâneurs posted by friends and all app users in a feed
 
#### Compose Screen
- [x] Create Flâneurs, either a photo or video with a caption, and drop it at your current location
 
#### Inbox Screen
- [ ] Allow users to see their picked up Flâneurs in an inbox
 
#### Detail Screen
- [x] Allow user to view the content associated with their Flâneurs
- [ ] Allow users to comment on picked up Flâneurs
- [x] Allow users to upvote/downvote their picked up Flâneurs 

### Optional User Stories
- [ ] Receieve notifications about flaneur subtypes you care about immediately
- [ ] Allow users to breakdown flaneur into different subtypes such as #history or #scenic or #artistic, and provide their own as well
- [ ] Ability to desiminate diffrent subtypes visually on the map and stream
- [ ] Ability to archive flaneurs in inbox
- [ ] Ability to delete flaneurs in inbox
- [x] Ability to view user profiles and see how many drops/picks up user has done, as well as view their timeline

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

### Sprint 1 Demo
![Flan](/images/flan_demo_1.gif)

- Facebook auth
- Parse backend
- Maps
- Camera

