# Zalo Marketing Miniapp – Database Data Dictionary

*Generated: 2025-10-31 09:32 UTC*

**How to read this document**  
For each table you’ll see: Purpose, Relationships, Columns, Indexes/Constraints, Typical Queries, and Data Lifecycle/Notes.
If a column name is listed under **Common Columns**, its meaning is shared across tables and not repeated in full.

### Common Columns
- `id` *(pk)*: Primary key (auto-increment).
- `account_id` *(fk → accounts.id)*: Tenant boundary key. Always filter queries by this to isolate tenant data.
- `created_at` *(timestamp)*: Record creation time (UTC).
- `updated_at` *(timestamp)*: Last update time (UTC).
- `deleted_at` *(timestamp, nullable)*: Soft-delete marker. Treat non-NULL as logically deleted.


---

## accounts

**Purpose:** Tenants (workspaces) that own all other records.

**Relationships:**
- Referenced by most tables via `account_id`.

**Columns:**
- `id`: Primary key.
- `name`: Account display name.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- PRIMARY KEY (`id`)
**Typical Queries / Usage:**
- List all active accounts.
- Fetch account settings before creating child records.
**Notes:** Bootstrap tenant; consider adding account-level settings in a separate table if needed.

---

## customers

**Purpose:** End users/contacts targeted by marketing automation.

**Relationships:**
- `account_id` → accounts.id
- Location FKs: `country_code` → location_countries.code; `state_code` → location_states.code; `city_code` → location_cities.code
- Referenced by: audienceMembers, journeyLogs, oaMessages, sessions, events, touchpoints, conversions, user_feedback, piiAccessLogs (as user_id).

**Columns:**
- `id`: Primary key.
- `account_id`: Tenant owner.
- `first_name`: Given name.
- `last_name`: Family name.
- `email`: Primary email for contactability/dedup.
- `phone`: Phone number (normalized if possible).
- `address`: Street address.
- `city_code`: FK to location_cities.code.
- `state_code`: FK to location_states.code.
- `country_code`: FK to location_countries.code.
- `marketing_consent`: Boolean flag for messaging permission.
- `consent_timestamp`: When consent was captured.
- `consent_source`: Channel/source where consent was obtained.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- UNIQUE (`account_id`,`email`) to avoid duplicates within tenant
- KEY (`account_id`), KEY (`created_at`), KEY (`updated_at`)
**Typical Queries / Usage:**
- Find customer by email/phone within tenant.
- Segment customers by location/consent.
**Notes:** Store PII; ensure encryption at rest and access logging via piiAccessLogs.

---

## customerZaloLinks

**Purpose:** Mapping between a customer and their Zalo account(s).

**Relationships:**
- `account_id` → accounts.id
- `customer_id` → customers.id
- `zalo_account_id` → zalo_accounts.id

**Columns:**
- `id`: Primary key.
- `account_id`: Tenant owner.
- `customer_id`: Customer linked.
- `zalo_account_id`: Linked Zalo account.
- `primary_flag`: Marks the primary Zalo account for this customer.
- `linked_at`: Timestamp when linkage happened.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- KEY (`account_id`), KEY (`customer_id`), KEY (`zalo_account_id`), KEY (`linked_at`)
- Optional UNIQUE (`account_id`,`customer_id`,`primary_flag`) to ensure at most one primary.
**Typical Queries / Usage:**
- Resolve the primary Zalo account for a customer before sending OA messages.
- Audit when a customer linked/unlinked Zalo.
**Notes:** If Zalo allows multiple accounts per user, use `primary_flag` for routing.

---

## zalo_accounts

**Purpose:** Zalo OA user tokens and profile metadata for API access on behalf of the tenant.

**Relationships:**
- `account_id` → accounts.id
- Referenced by: customerZaloLinks

**Columns:**
- `id`: Primary key.
- `account_id`: Tenant owner.
- `zalo_user_id`: Zalo's unique user identifier.
- `display_name`: Display name.
- `email`: Email (if provided).
- `avatar_url`: Profile image URL.
- `token_ciphertext`: Encrypted access token.
- `refresh_token_ciphertext`: Encrypted refresh token.
- `scopes`: Granted scopes list/string.
- `last_refreshed_at`: Last time token was refreshed.
- `token_expiry`: Token expiration time.
- `revoked_at`: When token/access was revoked.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- UNIQUE (`account_id`,`zalo_user_id`)
- KEY (`account_id`), KEY (`last_refreshed_at`)
**Typical Queries / Usage:**
- Load valid token for API call; refresh if near expiry.
- List connected Zalo accounts per tenant.
**Notes:** Store secrets encrypted; rotate tokens and redact in logs.

---

## marketing_campaigns

**Purpose:** Top-level campaign container with budget, dates, status, and timezone.

**Relationships:**
- `account_id` → accounts.id
- `product_id` → products.id
- `objective_id` → campaign_objectives.id
- `status_id` → campaign_statuses.id
- Referenced by: advertisements, oaMessages, events, touchpoints, conversions, experiments, campaign_channels, campaign_performance_metrics, user_feedback

**Columns:**
- `id`: Primary key.
- `account_id`: Tenant owner.
- `name`: Campaign name.
- `product_id`: Promoted product.
- `start_date`: Scheduled start date (UTC).
- `end_date`: Scheduled end date (UTC).
- `budget`: Total planned budget.
- `objective_id`: Business objective (awareness, traffic, etc.).
- `status_id`: Lifecycle status.
- `timezone`: Display/scheduling timezone.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- KEYs on (`account_id`,`start_date`,`end_date`,`product_id`,`objective_id`,`status_id`)
**Typical Queries / Usage:**
- List active campaigns by date window.
- Compute ROAS/CPA by campaign.
**Notes:** Timezone used for pacing and reporting cutoffs.

---

## audiences

**Purpose:** Reusable customer groups for targeting and sync to external platforms.

**Relationships:**
- `account_id` → accounts.id
- Referenced by: audienceMembers, audienceSyncs

**Columns:**
- `id`: Primary key.
- `account_id`: Tenant owner.
- `name`: Audience name.
- `type`: Static, dynamic (rule-based), lookalike, etc.
- `definitionJSON`: Rules / filters definition JSON.
- `status`: Build/sync status.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- KEY (`account_id`)
**Typical Queries / Usage:**
- Preview audience size by rules.
- Sync audience to Zalo or other destinations.
**Notes:** Keep definition JSON minimal and versioned if possible.

---

## audienceMembers

**Purpose:** Membership assignments of customers into audiences.

**Relationships:**
- `account_id` → accounts.id
- `audience_id` → audiences.id
- `customer_id` → customers.id

**Columns:**
- `id`: Primary key.
- `account_id`: Tenant owner.
- `audience_id`: Audience container.
- `customer_id`: Member customer.
- `joined_at`: When the customer entered the audience.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- UNIQUE (`audience_id`,`customer_id`)
- KEY (`account_id`), KEY (`joined_at`)
**Typical Queries / Usage:**
- Incremental sync: members joined since T.
- Check if a customer is in an audience before journey send.
**Notes:** Soft-delete to remove historical membership without losing history.

---

## audienceSyncs

**Purpose:** Audit of audience synchronizations to external targets.

**Relationships:**
- `account_id` → accounts.id
- `audience_id` → audiences.id

**Columns:**
- `id`: Primary key.
- `account_id`: Tenant owner.
- `audience_id`: Audience synced.
- `target`: Destination, e.g., Zalo OA, Ads, CSV.
- `status`: Queued/Running/Success/Failed.
- `synced_at`: When completed.
- `statsJSON`: Counts and error details.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- KEY (`account_id`), KEY (`audience_id`), KEY (`synced_at`)
**Typical Queries / Usage:**
- Show latest sync and errors per audience.
**Notes:** Use `statsJSON` for per-batch metrics.

---

## journeys

**Purpose:** Customer journey flows defining steps and timing windows.

**Relationships:**
- `account_id` → accounts.id
- Referenced by: journeySteps, journeyLogs

**Columns:**
- `id`: Primary key.
- `account_id`: Tenant owner.
- `name`: Journey name.
- `status`: Draft/Active/Paused/Ended.
- `start_at`: Activation time.
- `end_at`: Deactivation time.
- `timezone`: Scheduling timezone.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- KEY (`account_id`)
**Typical Queries / Usage:**
- List running journeys now by timezone.
**Notes:** Use together with `journeySteps` and `journeyLogs`.

---

## journeySteps

**Purpose:** Ordered steps inside a journey with action/config payloads.

**Relationships:**
- `account_id` → accounts.id
- `journey_id` → journeys.id
- Referenced by: journeyLogs

**Columns:**
- `id`: Primary key.
- `account_id`: Tenant owner.
- `journey_id`: Parent journey.
- `step_no`: Sequence order (1..N).
- `type`: Action type: wait, condition, send_oa, webhook, etc.
- `configJSON`: Parameters specific to type (e.g., wait duration).
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- UNIQUE (`journey_id`,`step_no`)
**Typical Queries / Usage:**
- Load steps ordered by `step_no` to execute runtime graph.
**Notes:** Validate `configJSON` schema per step type.

---

## journeyLogs

**Purpose:** Step-level execution logs per customer in a journey.

**Relationships:**
- `account_id` → accounts.id
- `journey_id` → journeys.id
- `step_id` → journeySteps.id
- `customer_id` → customers.id

**Columns:**
- `id`: Primary key.
- `account_id`: Tenant owner.
- `journey_id`: Journey ID.
- `step_id`: Step executed.
- `customer_id`: Customer involved.
- `event_time`: When the step executed.
- `outcome`: Result: sent, skipped, condition_true, error, etc.
- `detailJSON`: Raw details/errors for debugging.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- KEY (`account_id`,`event_time`), KEY (`journey_id`), KEY (`step_id`), KEY (`customer_id`)
**Typical Queries / Usage:**
- Audit a customer's path through a journey.
- Measure step conversion.
**Notes:** High volume; consider partitioning by month.

---

## oaTemplates

**Purpose:** Message templates for Zalo OA with variables and approval status.

**Relationships:**
- `account_id` → accounts.id
- Referenced by: oaMessages

**Columns:**
- `id`: Primary key.
- `account_id`: Tenant owner.
- `code`: Template code/key (unique per account).
- `content`: Template body.
- `variablesJSON`: Declared variables metadata.
- `status`: Draft/Approved/Rejected/Archived.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- UNIQUE (`account_id`,`code`)
**Typical Queries / Usage:**
- Resolve template and merge variables prior to send.
**Notes:** Keep version history separately if needed.

---

## oaMessages

**Purpose:** Inbound/outbound Zalo OA messages with delivery/read tracking.

**Relationships:**
- `account_id` → accounts.id
- `customer_id` → customers.id
- `campaign_id` → marketing_campaigns.id
- `template_id` → oaTemplates.id

**Columns:**
- `id`: Primary key.
- `account_id`: Tenant owner.
- `customer_id`: Recipient/sender customer.
- `campaign_id`: Optional attribution to campaign.
- `template_id`: Template used for outbound.
- `direction`: inbound | outbound.
- `content`: Final message content.
- `varsJSON`: Rendered variables snapshot.
- `status`: queued/sent/delivered/read/failed.
- `error_code`: Provider error code if failed.
- `sent_at`: When sent.
- `delivered_at`: When delivered.
- `read_at`: When read.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- KEY (`account_id`,`sent_at`), KEY (`customer_id`), KEY (`campaign_id`), KEY (`template_id`)
**Typical Queries / Usage:**
- Timeline of conversations per customer.
- KPI: delivery rate, read rate.
**Notes:** Keep content minimal; consider separate blob store for media.

---

## oaWebhookEvents

**Purpose:** Raw inbound webhook payloads from Zalo for auditing/reprocessing.

**Relationships:**
- `account_id` → accounts.id

**Columns:**
- `id`: Primary key.
- `account_id`: Tenant owner.
- `event_type`: Webhook event kind.
- `payloadJSON`: Raw provider payload.
- `received_at`: When received.
- `processed_at`: When processed.
- `status`: pending/success/failed.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- KEY (`account_id`,`received_at`)
**Typical Queries / Usage:**
- Reprocess failed events; reconcile with oaMessages.
**Notes:** Large JSON; retention policy recommended.

---

## events

**Purpose:** Product/marketing analytics events (page_view, click, add_to_cart, etc.).

**Relationships:**
- `account_id` → accounts.id
- `customer_id` → customers.id
- `session_id` → sessions.id
- `campaign_id` → marketing_campaigns.id
- `advertisement_id` → advertisements.id

**Columns:**
- `id`: Primary key (BIGINT for scale).
- `account_id`: Tenant owner.
- `customer_id`: Customer if known/identified.
- `session_id`: Session for anonymous/known browsing.
- `event_time`: Event timestamp.
- `event_name`: Event type name.
- `propertiesJSON`: Arbitrary event properties.
- `campaign_id`: Attribution link to campaign.
- `advertisement_id`: Attribution link to ad creative.
- `source`: SDK/source label.
- `device_id`: Client device identifier.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- KEY (`account_id`,`event_time`)
- KEY (`customer_id`), KEY (`session_id`), KEY (`campaign_id`), KEY (`advertisement_id`)
**Typical Queries / Usage:**
- Funnel, retention, attribution windows.
- Real-time event stream by tenant.
**Notes:** Very high volume; consider monthly partitions and TTL.

---

## sessions

**Purpose:** Analytics session boundaries and acquisition tags.

**Relationships:**
- `account_id` → accounts.id
- `customer_id` → customers.id
- Referenced by: events

**Columns:**
- `id`: Primary key.
- `account_id`: Tenant owner.
- `customer_id`: Optional known customer.
- `started_at`: Session start time.
- `ended_at`: Session end time.
- `device_id`: Client ID.
- `utm_source`: UTM source.
- `utm_medium`: UTM medium.
- `utm_campaign`: UTM campaign.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- KEY (`account_id`,`started_at`), KEY (`customer_id`)
**Typical Queries / Usage:**
- Build session-based analytics.
- Resolve last-touch attribution.
**Notes:** If using server events, ensure consistent device_id/sessionization.

---

## touchpoints

**Purpose:** Normalized marketing touch actions contributing to attribution.

**Relationships:**
- `account_id` → accounts.id
- `customer_id` → customers.id
- `campaign_id` → marketing_campaigns.id
- `advertisement_id` → advertisements.id

**Columns:**
- `id`: Primary key (BIGINT).
- `account_id`: Tenant owner.
- `customer_id`: Customer touched.
- `campaign_id`: Campaign cause.
- `advertisement_id`: Ad creative cause.
- `event_time`: When touch occurred.
- `type`: Impression/click/cta etc.
- `detailJSON`: Provider details.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- KEY (`account_id`,`event_time`)
- KEY (`customer_id`), KEY (`campaign_id`), KEY (`advertisement_id`)
**Typical Queries / Usage:**
- Multi-touch attribution modeling.
- Frequency capping logic.
**Notes:** Often derived from raw `events` + ad logs; maintain idempotent ingestion.

---

## conversions

**Purpose:** Business conversions (signup, purchase, lead) for attribution and KPIs.

**Relationships:**
- `account_id` → accounts.id
- `customer_id` → customers.id
- `campaign_id` → marketing_campaigns.id
- `advertisement_id` → advertisements.id

**Columns:**
- `id`: Primary key (BIGINT).
- `account_id`: Tenant owner.
- `customer_id`: Converted customer if known.
- `campaign_id`: Attributed campaign (if fixed).
- `advertisement_id`: Attributed ad (if fixed).
- `conversion_time`: When conversion happened.
- `type`: Conversion type code.
- `value`: Monetary value (if any; no payments table).
- `detailJSON`: Any metadata (order_id, etc.).
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- KEY (`account_id`,`conversion_time`)
- KEY (`customer_id`), KEY (`campaign_id`), KEY (`advertisement_id`)
**Typical Queries / Usage:**
- ROAS, CPA, LTV modeling.
- Attribution windows (e.g., 7d click, 1d view).
**Notes:** If purchases exist externally, sync order refs into `detailJSON` or a separate orders table.

---

## campaign_performance_metrics

**Purpose:** Aggregated performance metrics per campaign/ad per day/hour.

**Relationships:**
- `account_id` → accounts.id
- `campaign_id` → marketing_campaigns.id
- `advertisement_id` → advertisements.id

**Columns:**
- `id`: Primary key (BIGINT).
- `account_id`: Tenant owner.
- `campaign_id`: Campaign.
- `advertisement_id`: Ad creative.
- `metric_date`: Aggregation date (UTC).
- `metric_hour`: Hourly timestamp (if used).
- `reach`: Unique reach.
- `frequency`: Avg frequency.
- `engagements`: Engagement count.
- `sessions`: Session count attributed.
- `conversions`: Conversion count attributed.
- `revenue`: Revenue attributed.
- `cost`: Spend.
- `cpc`: Cost per click.
- `cpa`: Cost per acquisition.
- `roas`: Return on ad spend.
- `utm_source`: Optional dimension.
- `utm_medium`: Optional dimension.
- `utm_campaign`: Optional dimension.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- UNIQUE (`campaign_id`,`advertisement_id`,`metric_date`)
- KEYs on (`account_id`,`campaign_id`,`advertisement_id`,`metric_date`,`metric_hour`)
**Typical Queries / Usage:**
- Daily performance report & dashboards.
- Alerting on ROAS/CPC anomalies.
**Notes:** Treat as fact table; backfill-safe and idempotent.

---

## experiments

**Purpose:** A/B test containers linked to campaigns.

**Relationships:**
- `account_id` → accounts.id
- `campaign_id` → marketing_campaigns.id
- Referenced by: experimentVariants, experimentAssignments, advertisements (variant_id)

**Columns:**
- `id`: Primary key.
- `account_id`: Tenant owner.
- `name`: Experiment name.
- `campaign_id`: Campaign under test.
- `hypothesis`: Expected outcome.
- `start_date`: Start date.
- `end_date`: End date.
- `status`: Draft/Running/Stopped/Completed.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- KEY (`account_id`), KEY (`campaign_id`), KEY (`start_date`), KEY (`end_date`)
**Typical Queries / Usage:**
- List active experiments; compute variant lifts.
**Notes:** Govern assignment windows to avoid cross-contamination.

---

## experimentVariants

**Purpose:** Experiment arms/variants with traffic splits.

**Relationships:**
- `account_id` → accounts.id
- `experiment_id` → experiments.id
- Referenced by: experimentAssignments, advertisements.variant_id

**Columns:**
- `id`: Primary key.
- `account_id`: Tenant owner.
- `experiment_id`: Parent experiment.
- `code`: Short code for variant (A, B, …).
- `description`: Human-friendly description.
- `traffic_split`: Planned traffic percentage (0–100).
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- UNIQUE (`experiment_id`,`code`)
**Typical Queries / Usage:**
- Resolve variant ID by code during routing.
**Notes:** Enforce that splits sum to ~100 across active variants.

---

## experimentAssignments

**Purpose:** Customer-level assignment to variants for experiments.

**Relationships:**
- `account_id` → accounts.id
- `customer_id` → customers.id
- `experiment_id` → experiments.id
- `variant_id` → experimentVariants.id

**Columns:**
- `id`: Primary key (BIGINT).
- `account_id`: Tenant owner.
- `customer_id`: Assigned customer.
- `experiment_id`: Experiment.
- `variant_id`: Chosen variant.
- `assigned_at`: When assignment occurred.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- UNIQUE (`experiment_id`,`customer_id`)
- KEYs on (`account_id`,`customer_id`,`experiment_id`,`variant_id`,`assigned_at`)
**Typical Queries / Usage:**
- Join with outcomes to compute lift.
- Check consistent assignment across channels.
**Notes:** Immutable once assigned (except soft-delete for GDPR).

---

## assets

**Purpose:** Creative assets metadata (images/videos/files).

**Relationships:**
- `account_id` → accounts.id
- Referenced by: campaignAssets, adAssets

**Columns:**
- `id`: Primary key.
- `account_id`: Tenant owner.
- `type`: image/video/other.
- `url`: Storage URL.
- `checksum`: Integrity checksum.
- `width`: Pixel width (if applicable).
- `height`: Pixel height.
- `duration`: Duration seconds for video.
- `tags`: JSON array of tags.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- KEY (`account_id`)
**Typical Queries / Usage:**
- Find assets by tag/type for reuse.
**Notes:** Store renditions externally; keep metadata in DB.

---

## campaignAssets

**Purpose:** Link table between campaigns and assets.

**Relationships:**
- `account_id` → accounts.id
- `campaign_id` → marketing_campaigns.id
- `asset_id` → assets.id

**Columns:**
- `id`: Primary key.
- `account_id`: Tenant owner.
- `campaign_id`: Campaign.
- `asset_id`: Asset.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- UNIQUE (`campaign_id`,`asset_id`)
**Typical Queries / Usage:**
- List assets used by a campaign.
**Notes:** Many-to-many join helper.

---

## adAssets

**Purpose:** Link table between advertisements and assets.

**Relationships:**
- `account_id` → accounts.id
- `advertisement_id` → advertisements.id
- `asset_id` → assets.id

**Columns:**
- `id`: Primary key.
- `account_id`: Tenant owner.
- `advertisement_id`: Ad creative.
- `asset_id`: Asset.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- UNIQUE (`advertisement_id`,`asset_id`)
**Typical Queries / Usage:**
- Resolve creative bundle for ad delivery.
**Notes:** Many-to-many join helper.

---

## auditLogs

**Purpose:** Immutable audit trail of CRUD actions on entities.

**Relationships:**
- `account_id` → accounts.id

**Columns:**
- `id`: Primary key (BIGINT).
- `account_id`: Tenant owner.
- `entity_type`: Entity name (table).
- `entity_id`: Entity primary key.
- `action`: create/update/delete/etc.
- `actor_id`: Actor (customer/user) if tracked.
- `occurred_at`: When action occurred.
- `old_valuesJSON`: Before image.
- `new_valuesJSON`: After image.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- KEY (`account_id`,`occurred_at`), KEY (`entity_type`,`entity_id`)
**Typical Queries / Usage:**
- Change history for compliance.
- Debugging production issues.
**Notes:** High write volume; compress old rows; redact sensitive diffs.

---

## piiAccessLogs

**Purpose:** Logs of PII access events for compliance.

**Relationships:**
- `account_id` → accounts.id
- `user_id` → customers.id (as per schema)

**Columns:**
- `id`: Primary key (BIGINT).
- `account_id`: Tenant owner.
- `user_id`: Who accessed PII.
- `action`: Viewed/Exported/Downloaded/etc.
- `entity_type`: What entity was accessed.
- `entity_id`: PK of entity accessed.
- `at`: When access occurred.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- KEY (`account_id`,`at`), KEY (`user_id`)
**Typical Queries / Usage:**
- PII audit for incident response.
- Monthly compliance reports.
**Notes:** Ensure application writes a row for each sensitive view.

---

## suppressionLists

**Purpose:** Global suppressions per account (email/phone/zalo_id).

**Relationships:**
- `account_id` → accounts.id

**Columns:**
- `id`: Primary key.
- `account_id`: Tenant owner.
- `type`: email|phone|zalo_id|device_id etc.
- `value`: Value to suppress.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- UNIQUE (`account_id`,`type`,`value`)
**Typical Queries / Usage:**
- Pre-send check to avoid contacting suppressed identities.
**Notes:** Normalize values (lowercase email, E.164 phone).

---

## frequencyCaps

**Purpose:** Account-level frequency cap windows and rules.

**Relationships:**
- `account_id` → accounts.id

**Columns:**
- `id`: Primary key.
- `account_id`: Tenant owner.
- `windowJSON`: Window definitions (e.g., 24h/7d).
- `rulesJSON`: Rules per channel/event.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- KEY (`account_id`)
**Typical Queries / Usage:**
- Check send eligibility before enqueue.
**Notes:** Load into cache for low-latency enforcement.

---

## sendRateLimits

**Purpose:** Rate limiting rules (burst/sustained) per account or channel.

**Relationships:**
- `account_id` → accounts.id

**Columns:**
- `id`: Primary key.
- `account_id`: Tenant owner.
- `rulesJSON`: JSON describing rates (per minute/hour).
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- KEY (`account_id`)
**Typical Queries / Usage:**
- Throttle job dispatchers and outbound sends.
**Notes:** Keep a synchronized counter in Redis or similar.

---

## links

**Purpose:** Short-links with optional UTM metadata.

**Relationships:**
- `account_id` → accounts.id
- Referenced by: linkClicks, qrCodes

**Columns:**
- `id`: Primary key.
- `account_id`: Tenant owner.
- `target_url`: Destination URL.
- `short_code`: Short id code (unique).
- `utm_source`: UTM source.
- `utm_medium`: UTM medium.
- `utm_campaign`: UTM campaign.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- UNIQUE (`short_code`), KEY (`account_id`)
**Typical Queries / Usage:**
- Resolve target from short_code at click time.
**Notes:** Consider reserved namespaces and abuse prevention.

---

## linkClicks

**Purpose:** Click logs for short-links with optional customer/device.

**Relationships:**
- `account_id` → accounts.id
- `link_id` → links.id
- `customer_id` → customers.id

**Columns:**
- `id`: Primary key (BIGINT).
- `account_id`: Tenant owner.
- `link_id`: Short-link clicked.
- `clicked_at`: When clicked.
- `customer_id`: Who clicked (if known).
- `device_id`: Device fingerprint.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- KEY (`account_id`,`clicked_at`), KEY (`link_id`), KEY (`customer_id`)
**Typical Queries / Usage:**
- CTR analysis per campaign/asset.
- Identify devices/users engaging with links.
**Notes:** Use batching and deduplication for high-volume links.

---

## qrCodes

**Purpose:** QR codes mapped to short-links for offline-to-online tracking.

**Relationships:**
- `account_id` → accounts.id
- `link_id` → links.id

**Columns:**
- `id`: Primary key.
- `account_id`: Tenant owner.
- `link_id`: Linked short-link.
- `image_url`: Generated QR image location.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- KEY (`account_id`), KEY (`link_id`)
**Typical Queries / Usage:**
- List QR codes per campaign/venue.
**Notes:** Regenerate if brand changes; cache images via CDN.

---

## campaign_statuses

**Purpose:** Lookup of campaign statuses per account.

**Relationships:**
- `account_id` → accounts.id
- Referenced by: marketing_campaigns

**Columns:**
- `id`: Primary key.
- `account_id`: Tenant owner.
- `name`: Status name.
- `description`: Optional description.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- KEY (`account_id`)
**Typical Queries / Usage:**
- Join to label campaigns in dashboards.
**Notes:** Seed with standard statuses.

---

## ad_statuses

**Purpose:** Lookup of ad statuses per account.

**Relationships:**
- `account_id` → accounts.id
- Referenced by: advertisements

**Columns:**
- `id`: Primary key.
- `account_id`: Tenant owner.
- `name`: Status name.
- `description`: Optional description.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- KEY (`account_id`)
**Typical Queries / Usage:**
- Join to label ads in dashboards.
**Notes:** Seed with 'draft', 'active', 'paused', 'ended'.

---

## campaign_objectives

**Purpose:** Lookup of campaign objectives per account.

**Relationships:**
- `account_id` → accounts.id
- Referenced by: marketing_campaigns

**Columns:**
- `id`: Primary key.
- `account_id`: Tenant owner.
- `name`: Objective name.
- `description`: Description/help text.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- KEY (`account_id`)
**Typical Queries / Usage:**
- Join to display campaign objective.
**Notes:** Seed at account creation.

---

## channel_platforms

**Purpose:** Lookup of social/ad platforms (e.g., Zalo, Facebook).

**Relationships:**
- `account_id` → accounts.id
- Referenced by: social_media_channels

**Columns:**
- `id`: Primary key.
- `account_id`: Tenant owner.
- `name`: Platform name.
- `description`: Optional description.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- KEY (`account_id`)
**Typical Queries / Usage:**
- List supported platforms.
**Notes:** Use consistent naming for cross-channel reporting.

---

## social_media_channels

**Purpose:** Specific channels within a platform for the account.

**Relationships:**
- `account_id` → accounts.id
- `platform_id` → channel_platforms.id
- Referenced by: campaign_channels, advertisements

**Columns:**
- `id`: Primary key.
- `account_id`: Tenant owner.
- `name`: Channel/page name.
- `platform_id`: Which platform.
- `url`: Public URL.
- `description`: Notes.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- KEY (`account_id`), KEY (`platform_id`)
**Typical Queries / Usage:**
- Select channel for ad publishing.
**Notes:** May map to an external page/app ID.

---

## campaign_channels

**Purpose:** Which channels a campaign runs on.

**Relationships:**
- `account_id` → accounts.id
- `campaign_id` → marketing_campaigns.id
- `channel_id` → social_media_channels.id

**Columns:**
- `id`: Primary key.
- `account_id`: Tenant owner.
- `campaign_id`: Campaign.
- `channel_id`: Channel.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- UNIQUE (`campaign_id`,`channel_id`)
**Typical Queries / Usage:**
- List channels for a campaign.
**Notes:** Many-to-many helper.

---

## products

**Purpose:** Products promoted by campaigns.

**Relationships:**
- `account_id` → accounts.id
- Referenced by: product_variants, marketing_campaigns, user_feedback

**Columns:**
- `id`: Primary key.
- `account_id`: Tenant owner.
- `name`: Product name.
- `description`: Product description.
- `category`: Product category.
- `price`: Reference price.
- `launch_date`: Launch date.
- `status`: Lifecycle status.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- KEY (`account_id`)
**Typical Queries / Usage:**
- Attach products to campaigns and feedback.
**Notes:** For full commerce, add separate catalog tables.

---

## product_variants

**Purpose:** Variants/SKUs of products.

**Relationships:**
- `account_id` → accounts.id
- `product_id` → products.id
- Referenced by: advertisements

**Columns:**
- `id`: Primary key.
- `account_id`: Tenant owner.
- `product_id`: Parent product.
- `sku`: Stock keeping unit (unique per product).
- `name`: Variant name.
- `price`: Variant price.
- `attributes`: JSON map of attributes (color/size/...).
- `status`: Active/Inactive.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- UNIQUE (`product_id`,`sku`)
**Typical Queries / Usage:**
- Select variant for ad creative.
**Notes:** Keep attributes schema light and consistent.

---

## advertisements

**Purpose:** Ad creatives/placements per campaign and channel.

**Relationships:**
- `account_id` → accounts.id
- `campaign_id` → marketing_campaigns.id
- `channel_id` → social_media_channels.id
- `product_variant_id` → product_variants.id
- `status_id` → ad_statuses.id
- `variant_id` → experimentVariants.id (optional)

**Columns:**
- `id`: Primary key.
- `account_id`: Tenant owner.
- `campaign_id`: Campaign.
- `channel_id`: Channel.
- `product_variant_id`: Promoted variant.
- `title`: Ad title.
- `content`: Body text.
- `media_url`: Creative media URL.
- `start_date`: Start date.
- `end_date`: End date.
- `budget`: Planned budget.
- `status_id`: Status lookup.
- `variant_id`: Experiment variant linkage.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- KEYs on (`account_id`,`campaign_id`,`channel_id`,`product_variant_id`,`status_id`,`variant_id`,`start_date`,`end_date`)
**Typical Queries / Usage:**
- List live ads today.
- Join to performance metrics.
**Notes:** Immutable content snapshots recommended for audit.

---

## user_feedback

**Purpose:** Customer feedback/NPS linked to product/campaign.

**Relationships:**
- `account_id` → accounts.id
- `customer_id` → customers.id
- `product_id` → products.id
- `campaign_id` → marketing_campaigns.id
- Referenced by: attachments

**Columns:**
- `id`: Primary key.
- `account_id`: Tenant owner.
- `customer_id`: Submitting customer.
- `product_id`: Related product.
- `campaign_id`: Related campaign.
- `feedback_type`: Type e.g., complaint, praise, suggestion.
- `feedback_text`: Free-form text.
- `rating`: Star/score rating.
- `nps_score`: Net Promoter Score (-100..100 or 0..10 depending).
- `tags`: JSON array of tags/labels.
- `submitted_at`: Submission timestamp.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- UNIQUE (`customer_id`,`campaign_id`,date(`submitted_at`))
- KEYs on (`account_id`,`customer_id`,`product_id`,`campaign_id`,`submitted_at`)
**Typical Queries / Usage:**
- Track NPS over time per campaign/product.
- Surface top issues via tags.
**Notes:** Sanitize content; consider sentiment analysis pipeline.

---

## attachments

**Purpose:** Files attached to feedback (images, docs).

**Relationships:**
- `account_id` → accounts.id
- `feedback_id` → user_feedback.id

**Columns:**
- `id`: Primary key.
- `account_id`: Tenant owner.
- `feedback_id`: Parent feedback.
- `file_url`: File storage URL.
- `file_type`: MIME type.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- KEY (`account_id`), KEY (`feedback_id`)
**Typical Queries / Usage:**
- Fetch images for review ticket.
**Notes:** Virus-scan uploads and set signed URLs.

---

## location_countries

**Purpose:** Country codes and names.

**Relationships:**
- Referenced by: customers.country_code, location_states.country_code

**Columns:**
- `id`: Primary key.
- `code`: Country code (ISO-like).
- `name`: Country name.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- UNIQUE (`code`)
**Typical Queries / Usage:**
- Join for display names.
**Notes:** Seed from ISO 3166.

---

## location_states

**Purpose:** First-level admin divisions.

**Relationships:**
- `country_code` → location_countries.code
- Referenced by: customers.state_code, location_cities.state_code

**Columns:**
- `id`: Primary key.
- `country_code`: Parent country code.
- `code`: State/province code.
- `name`: State/province name.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- KEY (`country_code`), UNIQUE (`code`)
**Typical Queries / Usage:**
- Join for display names.
**Notes:** Use consistent codes across imports.

---

## location_cities

**Purpose:** City/municipality reference data.

**Relationships:**
- `state_code` → location_states.code
- Referenced by: customers.city_code

**Columns:**
- `id`: Primary key.
- `state_code`: Parent state/province code.
- `code`: City code.
- `name`: City name.
- `created_at`: See Common Columns.
- `updated_at`: See Common Columns.
- `deleted_at`: See Common Columns.
**Indexes & Constraints:**
- KEY (`state_code`), UNIQUE (`code`)
**Typical Queries / Usage:**
- Join for display names.
**Notes:** Scope codes uniquely across states.