# Cine-Catalog

USER_DATA:
-Username: sadmin123
-Password: sadmin123


Cine-Catalog is a web-based movie catalog application designed to create a community for cinema enthusiasts.

The platform allows users to explore, search, and contribute movie-related content while providing role-based access, subscription-based functionality, reviews, comments, and notifications.

The project follows a clean MVC architecture and has been extended with a dedicated **Review Service microservice** responsible for managing movie reviews and comments.

---

## Overview

Cine-Catalog consists of two main applications:

* **Main Application** — responsible for users, authentication, authorization, movies, actors, subscriptions, notifications, and administration.
* **Review Service** — a dedicated microservice responsible for reviews and comments.

The Main Application communicates with the Review Service using **OpenFeign**.

The Review Service does not contain its own user authentication, login, or user-management functionality. Authentication and authorization are handled by the Main Application.

---

## Technologies Used

### Main Application

* Java 17
* Spring Boot 3.4.0
* Spring MVC
* Thymeleaf
* Spring Security
* Spring Data JPA
* Hibernate / JPA
* Maven
* MySQL
* OpenFeign
* Bean Validation
* Session-based authentication
* Spring Events
* Scheduled Tasks / Cron
* Global Controller Advice

### Review Service

* Java 17
* Spring Boot 3.4.0
* Spring MVC
* Spring Data JPA
* Hibernate / JPA
* Maven
* MySQL
* OpenFeign integration
* Bean Validation

---

# Main Features

## User Management

Users can:

* Create and manage their profiles
* Update personal information
* Purchase subscriptions
* Upload movies and actors if they have an active subscription
* Publish reviews and comments if they have an active subscription
* Access additional functionality based on their subscription status
* View, edit, and manage only the movies they have personally uploaded
* View and manage their own reviews

---

## Subscriptions

Subscriptions:

* Have a specific duration
* Expire after the selected period
* Cannot be purchased again while an active subscription exists
* Provide access to current and future platform features
* Allow regular users to publish reviews and comments while active

Administrators can create reviews and comments without having an active subscription.

When a user purchases a subscription, the Main Application creates a notification informing the user about the subscription purchase.

---

# Role Management

The application supports three user roles:

## Super Admin

Super administrators can:

* Create administrators
* Remove administrators
* Change user roles
* Manage all users
* Activate or deactivate accounts
* Manage platform functionality
* Manage their own uploaded movies

Super administrators cannot manage movies uploaded by other users through the personal movie-management functionality.

---

## Admin

Administrators can:

* Manage regular users
* Deactivate user accounts
* Create movies
* Edit movies
* Delete movies
* Create and manage actors
* Manage reviews and comments through the administration panel
* Delete reviews
* Delete comments
* Provide a deletion reason when removing reviews or comments

Administrators cannot modify administrator or super administrator accounts.

Administrators can also create reviews and comments without requiring an active subscription.

---

## User

Regular users can:

* Browse the movie catalog
* Search and filter movies
* Publish movies and actors with an active subscription
* Publish reviews with an active subscription
* Publish comments with an active subscription
* Edit their own reviews
* Delete their own reviews and comments
* Search for reviews using available search parameters
* Manage allowed profile information
* Manage only movies they have personally uploaded

---

# Movie Catalog

The platform provides:

* Movie creation
* Movie editing
* Movie deletion
* Actor management
* Movie details pages
* Information about the user who published a movie
* Pagination support

Users can search and filter movies by:

* Title
* Release year
* Genre
* Country

Sorting options include:

* Newest movies
* Release year
* Alphabetical order

Multiple filters can be combined with sorting to provide flexible searching.

---

# Review Service

Cine-Catalog has been extended with a dedicated microservice called:

**`review-service`**

The purpose of this microservice is to separate review and comment management from the Main Application.

The Review Service is responsible for:

* Creating reviews
* Editing reviews
* Deleting reviews
* Creating comments
* Deleting comments
* Searching reviews
* Managing review-related data
* Managing comment-related data

The Main Application communicates with the Review Service using **OpenFeign clients**.

---

## Review Service Architecture

The Review Service contains two main domain entities:

* `Review`
* `Comment`

The service does not contain:

* User login
* User registration
* User authentication
* User session management
* Subscription management
* User role management

These responsibilities remain entirely within the Main Application.

There is also no JWT-based authentication between the applications.

---

# Review Entity

Reviews contain information necessary to associate them with movies and their publishers.

The Review Service stores identifiers and basic information received from the Main Application instead of maintaining a direct JPA relationship with the Main Application's entities.

Relevant movie and publisher information includes:

```java
@Column(nullable = false)
private UUID movieId;

@Column(nullable = false)
private String movieTitle;

@Column(nullable = false)
private UUID publisherId;

@Column(nullable = false)
private String publisherUsername;
```

This approach allows the Review Service to remain independent from the Main Application's `Movie` and `User` entities.

---

# Comment Entity

Comments are associated with the user who published them.

The Comment entity stores the publisher information directly:

```java
@Column(nullable = false)
private UUID publisherId;

@Column(nullable = false)
private String publisherUsername;

private boolean isDeleted;
```

The `publisherId` and `publisherUsername` allow the Review Service to identify the comment author without maintaining a direct dependency on the Main Application's `User` entity.

The `isDeleted` property is used to track deleted comments.

---

# Reviews

Users with an active subscription can:

* Create reviews
* Edit their own reviews
* Delete their own reviews
* View their own reviews
* Search for reviews
* Write comments on reviews

Administrators can also create and manage reviews without an active subscription.

---

# Comments

Users with an active subscription can:

* Create comments
* Delete their own comments
* View comments associated with reviews

Administrators can also create comments without an active subscription.

Administrators have additional permissions through the administration panel and can delete comments created by other users.

---

# Review Search

The application provides review search functionality.

Both regular users and administrators can search for reviews using the available search parameters.

The search functionality allows users to find specific reviews without having to manually browse through all available reviews.

---

# Review and Comment Administration

A dedicated administration panel has been added to the Main Application for managing reviews and comments.

Administrators can:

* View reviews
* View comments
* Delete reviews
* Delete comments
* Specify a reason when deleting content

When an administrator deletes a review or comment, the affected user immediately receives an internal notification explaining that their content has been removed and providing the deletion reason.

---

# Notifications

A notification system has been implemented inside the **Main Application**.

Notifications are stored and managed by the Main Application.

The system provides notifications for important user-related events.

Currently supported notification events include:

* Successful subscription purchase
* Review deletion by an administrator
* Comment deletion by an administrator
* Upcoming subscription expiration

---

## Notification Management

Users can:

* View their notifications
* Mark notifications as read
* Delete notifications

The application also provides a notification indicator in the navigation bar.

A **Global Notification Advice** is used to determine and display the number of unread notifications next to the notification bell icon.

This allows users to immediately see when they have new notifications.

---

# Review and Comment Deletion Events

The notification system uses application events to decouple review/comment deletion from notification creation.

Two dedicated events are used to track:

* Review deletion
* Comment deletion

When an administrator deletes a review or comment:

1. The deletion is performed through the administration functionality.
2. The corresponding event is triggered.
3. The Main Application receives the event.
4. A notification is created for the affected user.
5. The user can see the notification through the notification bell.

The administrator must provide a reason for the deletion, which is included in the notification sent to the user.

---

# Subscription Expiration Notifications

A scheduled task has been implemented in the Main Application to monitor subscription expiration.

The application uses a **Cron expression** to execute the scheduled check every day at **00:01**.

The scheduled process checks whether users have subscriptions that will expire within the next **3 days**.

If a user's subscription is approaching expiration:

1. The scheduled task detects the upcoming expiration.
2. A notification is created for the user.
3. The user can see the notification through the notification system.

This provides users with an early reminder before their subscription expires.

---

# Spring Security

Spring Security has been integrated into the Main Application.

Security is responsible for:

* User authentication
* Authorization
* Role-based access control
* Protecting administration functionality
* Protecting subscription-dependent functionality
* Restricting access to user-specific resources

The Review Service does not implement its own login or user authentication system.

There is no JWT authentication between the Main Application and the Review Service.

The Main Application remains the central point responsible for user security and access control.

---

# Communication Between Applications

The Main Application communicates with the Review Service through **OpenFeign**.

Feign clients are used to provide communication between the two applications without tightly coupling their domain models.

The Main Application contains the necessary:

* Feign clients
* DTOs
* Controller methods
* Service methods
* Integration logic

required to communicate with the Review Service.

The Review Service manages its own review and comment domain while the Main Application remains responsible for users, subscriptions, roles, and notifications.

---

# Microservice Responsibilities

| Responsibility      | Main Application | Review Service |
| ------------------- | :--------------: | :------------: |
| User management     |         ✓        |                |
| User authentication |         ✓        |                |
| Spring Security     |         ✓        |                |
| Roles               |         ✓        |                |
| Subscriptions       |         ✓        |                |
| Movies              |         ✓        |                |
| Actors              |         ✓        |                |
| Notifications       |         ✓        |                |
| Reviews             |                  |        ✓       |
| Comments            |                  |        ✓       |
| Review search       |                  |        ✓       |
| Comment management  |                  |        ✓       |
| Review management   |                  |        ✓       |
| Feign integration   |         ✓        |        ✓       |

---

# Application Architecture

The project follows a layered MVC architecture.

### Main Application

* **Controllers** — handle HTTP requests and application flow
* **Services** — contain business logic
* **Repositories** — manage database communication
* **DTOs** — transfer and validate data
* **Entities** — represent database models
* **Security** — authentication and authorization
* **Events** — handle application events
* **Scheduled Tasks** — perform periodic subscription checks
* **Feign Clients** — communicate with the Review Service
* **Global Controller Advice** — handles application-wide exceptions and notification-related data

### Review Service

* **Controllers** — handle review and comment HTTP requests
* **Services** — contain review and comment business logic
* **Repositories** — manage review and comment persistence
* **DTOs** — transfer and validate review/comment data
* **Entities** — represent review and comment data
* **Search functionality** — handles review searching

---

# Main Entities

The Main Application contains the following main entities:

* `User`
* `Movie`
* `Actor`
* `Subscription`
* `Notification`

The Review Service contains:

* `Review`
* `Comment`

---

# Enums

The Main Application uses enums such as:

* `Genre`
* `ReleaseYear`
* `Country`
* `Role`

---

# Validation and Error Handling

The application uses **Bean Validation** to ensure correct user input and prevent invalid data.

Validation is applied to DTOs and request data before processing.

Global error handling is implemented through `GlobalControllerAdvice`.

The global exception handling mechanism catches application exceptions and provides user-friendly error pages instead of displaying default Spring error pages.

The Main Application also uses global notification-related functionality to display the current number of unread notifications in the application's navigation interface.

---

# Data Ownership and Service Independence

The Review Service intentionally does not create direct JPA relationships with `User` or `Movie` entities from the Main Application.

Instead, reviews store values such as:

* `movieId`
* `movieTitle`
* `publisherId`
* `publisherUsername`

This keeps the Review Service independent from the Main Application's database entities.

The Review Service therefore has its own domain model and database responsibilities while the Main Application remains responsible for the core Cine-Catalog platform.

---

# Project Structure

The project is divided into two applications:

```text
Cine-Catalog/
│
├── main-app/
│   ├── controllers/
│   ├── services/
│   ├── repositories/
│   ├── entities/
│   ├── dto/
│   ├── security/
│   ├── events/
│   ├── clients/
│   └── ...
│
└── review-service/
    ├── controllers/
    ├── services/
    ├── repositories/
    ├── entities/
    ├── dto/
    └── ...
```

The exact package structure may vary depending on the implementation.

---

# Super Admin Account

The project contains a predefined Super Admin account for administration purposes:

```text
Username: sadmin123
Password: sadmin123
```

For production environments, default credentials should be changed and sensitive credentials should never be stored directly in source code.

---

# Future Improvements

Planned improvements include:

* Advanced content moderation system
* Additional subscription-based features
* More community features
* Extended user interaction functionality
* Further improvements to security
* Improved microservice scalability
* More sophisticated notification functionality
* Additional review and comment moderation capabilities
* Improved service-to-service security
* Additional asynchronous communication between services
* Further improvements to application scalability and performance

---

# How to Run

## Requirements

* Java 17+
* Spring Boot 3.4.0
* Maven
* MySQL

## Main Application

1. Clone the repository.
2. Configure the Main Application database connection in `application.properties` or `application.yml`.
3. Configure the Review Service connection.
4. Build the Main Application using Maven.
5. Start the Main Application.

## Review Service

1. Configure the Review Service database connection.
2. Configure the required application properties.
3. Build the Review Service using Maven.
4. Start the Review Service.

Both applications must be running for review and comment functionality to work correctly.

---

# Summary

Cine-Catalog combines a traditional Spring MVC web application with a dedicated microservice architecture for community-driven movie reviews and comments.

The **Main Application** remains responsible for:

* Users
* Authentication
* Authorization
* Roles
* Movies
* Actors
* Subscriptions
* Notifications
* Administration

The **Review Service** is responsible for:

* Reviews
* Comments
* Review search
* Review and comment management

Communication between the two applications is handled through **OpenFeign**, while notifications and user-related functionality remain centralized in the Main Application.

This architecture provides a clear separation of responsibilities and creates a foundation for future expansion into additional microservices and community-oriented features.
