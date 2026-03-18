# PopularVote
A platform that connects politicians with their constituents. 
By enabling politicians to publicly share policy proposals and allowing citizens to vote on them directly, 
the platform fosters a more issues-based political landscape, 
reducing the necessity for rigid adherence to party lines.

## Features

### Citizen Management
- **Citizen Profiles**: Create and manage detailed citizen profiles, including political affiliation and geographic information.
- **Geographic Targeting**: Associate citizens with federal electoral districts, municipalities, provinces, and territories.

### Policy & Governance
- **Policy Proposals**: Politicians can create and share detailed policy proposals with the public.
- **Opinion Sharing**: Citizens can share opinions and feedback on specific policies to foster discussion.
- **Direct Voting**: A transparent mechanism for citizens to vote directly on policy proposals, providing clear feedback to politicians.
- **Policy Bookmarking**: Citizens can bookmark policies they are interested in following.

### Political Engagement
- **Political Party Management**: Support for federal and provincial political parties, including their association with specific provinces.
- **Politician Verification**: A dedicated process for verifying politicians to ensure the platform's integrity.
- **Roles and Permissions**: Role-based access control (e.g., CITIZEN, POLITICIAN) to manage platform interactions.

### Technical Foundation
- **Reactive API**: Built with Java, Spring Boot, and reactive technologies (Spring Data R2DBC) for high performance and scalability.
- **Scalable Database**: Optimized for SQL databases, tested on MySQL and AWS Aurora.
- **Automated Migrations**: Uses Flyway for consistent and reliable database schema management.

I've written the API using Java and Spring using reactive technologies. I've opted for a SQL database; locally it's tested on MySQL, while in the cloud I use AWS Aurora DB.

Checkout [deploy](/deploy) for AWS deployment instructions.

Checkout [database](/api/database) for the database migration instructions and the database schema.

Checkout [api](/api) for instructions to run the API.

Checkout [ui](/ui) for instructions to run the UI.
