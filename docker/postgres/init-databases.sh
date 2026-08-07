#!/bin/bash
set -euo pipefail

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
CREATE DATABASE auth_db;
CREATE DATABASE user_db;
CREATE DATABASE profile_db;
CREATE DATABASE fashion_discovery_db;
CREATE DATABASE product_db;
CREATE DATABASE search_db;
CREATE DATABASE recommendation_db;
CREATE DATABASE outfit_detection_db;
CREATE DATABASE image_processing_db;
CREATE DATABASE visual_search_db;
CREATE DATABASE ai_stylist_db;
CREATE DATABASE virtual_tryon_db;
CREATE DATABASE moodboard_db;
CREATE DATABASE shopping_db;
CREATE DATABASE order_db;
CREATE DATABASE payment_db;
CREATE DATABASE inventory_db;
CREATE DATABASE brand_integration_db;
CREATE DATABASE notification_db;
CREATE DATABASE analytics_db;
CREATE DATABASE media_db;
EOSQL
