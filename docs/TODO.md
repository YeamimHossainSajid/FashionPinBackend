# Fashion Pin Backend - Project Roadmap & TODOs

This document tracks completed implementation phases and remaining TODOs for future development phases of **Fashion Pin**.

---

## Completed Phases

### Phase 1: Authentication + User Management + Profile Management (Completed)
- **`auth-service`**:
  - `UserAccount` domain model (`id`, `email`, `passwordHash`, `status`, `roles`, `createdAt`, `updatedAt`).
  - BCrypt password hashing.
  - JWT access token generation & claims extraction.
  - Refresh token management in PostgreSQL & Redis with token rotation and revocation.
  - **Transactional Outbox Pattern**: Event publishing (`UserRegisteredEvent`) via outbox table & scheduler.
  - `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/refresh`, `POST /api/auth/logout`.
- **`user-service`**:
  - `User` domain model (`id`, `email`, `status`, `createdAt`, `updatedAt`).
  - Idempotent event consumer (`UserRegisteredConsumer`) listening on `fashionpin.user.registered.v1`.
  - Transactional outbox relaying `UserCreatedEvent` to `fashionpin.user.created.v1`.
  - `GET /api/users/{id}`, `GET /api/users/me`, `PATCH /api/users/me`, `DELETE /api/users/me`.
- **`profile-service`**:
  - `Profile` domain model (`id`, `userId`, `displayName`, `username`, `bio`, `profileImage`, `gender`, `dateOfBirth`, `location`, `createdAt`, `updatedAt`).
  - Idempotent event consumer (`UserCreatedConsumer`) initializing user profiles.
  - `GET /api/profiles/{userId}`, `GET /api/profiles/me`, `PATCH /api/profiles/me`.
- **`api-gateway`**:
  - Edge routing for `/api/auth/**`, `/api/users/**`, `/api/profiles/**`.
  - Gateway `JwtAuthenticationFilter` for token validation and header enrichment (`X-User-Id`, `X-User-Roles`).

### Phase 2: Product & Fashion Discovery Domain (Completed)
- **`media-service`**:
  - `Media` domain model (`id`, `ownerId`, `mediaType`, `storageKey`, `originalFilename`, `contentType`, `fileSize`, `width`, `height`, `status`, `createdAt`, `updatedAt`).
  - Hexagonal object storage abstraction (`StoragePort` interface and `LocalStorageAdapter`).
  - Endpoints: `POST /api/media/upload`, `GET /api/media/{id}`, `GET /api/media/{id}/metadata`, `DELETE /api/media/{id}`.
  - Outbox events: `MediaCreatedEvent`, `MediaReadyEvent`, `MediaDeletedEvent`.
- **`brand-integration-service`**:
  - `Brand` domain model (`id`, `name`, `slug` [unique], `description`, `logoMediaId`, `website`, `status`, `createdAt`, `updatedAt`).
  - Hexagonal integration abstractions (`BrandIntegrationPort`, `BrandCatalogProvider`).
  - Endpoints: `POST /api/brands` (ADMIN), `GET /api/brands/{id}` (Public), `GET /api/brands` (Public), `PATCH /api/brands/{id}` (ADMIN), `DELETE /api/brands/{id}` (ADMIN).
  - Outbox events: `BrandCreatedEvent`, `BrandUpdatedEvent`, `BrandDeletedEvent`.
- **`product-service`**:
  - `Product` aggregate model with fashion attributes (`gender`, `color`, `size`, `material`, `pattern`, `style`, `season`, `occasion`, `fit`).
  - OpenFeign synchronous brand validation (`BrandServiceClient`).
  - Redis caching (`@Cacheable` and `@CacheEvict`).
  - Endpoints: `POST /api/products` (ADMIN), `GET /api/products/{id}` (Public), `GET /api/products` (Public, paginated & filtered), `PATCH /api/products/{id}` (ADMIN), `DELETE /api/products/{id}` (ADMIN).
  - Outbox events: `ProductCreatedEvent`, `ProductUpdatedEvent`, `ProductDeletedEvent`.

---

## Remaining TODOs for Future Phases

### Phase 3: Moodboards & Social Graph Domain
- [ ] User follow/following graph & social interactions (`profile-service`).
- [ ] Curation moodboards, saved pins, and collections (`moodboard-service`).

### Phase 4: AI & Computer Vision Domain
- [ ] Image processing pipeline & feature extraction (`image-processing-service`, `media-service`).
- [ ] Visual search engine (`visual-search-service`).
- [ ] AI stylist recommendation engine (`ai-stylist-service`, `recommendation-service`).
- [ ] Virtual try-on stubs and rendering (`virtual-tryon-service`).
- [ ] Outfit detection from uploaded images (`outfit-detection-service`).

### Phase 5: Commerce, Orders & Payments Domain
- [ ] Shopping cart & bag management (`shopping-service`).
- [ ] Inventory locking & management (`inventory-service`).
- [ ] Order checkout & lifecycle management (`order-service`).
- [ ] Payment gateway integration & webhook handling (`payment-service`).
- [ ] Brand partner integration APIs (`brand-integration-service`).

### Phase 6: Platform Utilities & Analytics
- [ ] Real-time push & email notifications (`notification-service`).
- [ ] User behavior & platform analytics event processing (`analytics-service`).
