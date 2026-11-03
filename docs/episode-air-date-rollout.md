# Episode air date rollout (FE #52 / BE #77)

## Contract

Episode responses add nullable `airDate` as `yyyy-MM-dd`. Existing fields remain unchanged.
TMDB `air_date` is stored as Oracle DATE through LocalDate, with no time component.
Missing/unscheduled dates remain null. FE shows dates through the current Asia/Seoul calendar
day; this is date-level availability, not an exact broadcast-time guarantee.

## Expand and verify

1. Validate V7 on a disposable development Oracle database. Existing rows, primary keys,
   and relations must be unchanged; AIR_DATE must be nullable.
2. Deploy the additive migration and backend before the frontend. Old backend/frontend
   versions ignore the new nullable column/response property.
3. Backfill existing episodes before enabling the frontend filter if existing catalogue
   visibility must be preserved. Until then, the approved FE policy hides unknown dates.
4. Verify past/today/future/null responses, then enable the FE filter.

No production migration or backfill is executed by this change.

## Existing data backfill

Do not use `force=true` import to backfill: it deletes/recreates seasons and episodes.
Prepare a separately reviewed job using CONTENT.TMDB_ID plus existing SEASON_NO and
EPISODE_NO to read the matching TMDB season. Update only AIR_DATE on the existing
EPISODE_ID, using bind parameters. Keep a per-episode old/new-date manifest, skip and
report missing or failed source records, and commit in bounded batches. Setting the
same date again is idempotent. Never infer dates from thumbnails, summaries, or episode
numbers. Verify affected row counts and unchanged IDs/relationships before rollout.

## Rollback

Roll back the FE filter first to restore previous catalogue visibility, then roll back
the backend if needed. Leave the additive column and V7 history in place; the old code
ignores them. Do not edit an applied migration or drop the column. Any backfill rollback
must use its reviewed old/new-date manifest and must not delete episodes.
