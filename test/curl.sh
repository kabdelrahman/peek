# staging

# curl \
# 	-H "Authorization: Bearer `zign token -n nakadi nakadi.event_stream.read`" \
# 	-H 'Content-Type: application/json' \
# 	-H 'X-Nakadi-Cursors: [{"partition":"0","offset":"BEGIN"}]' \
# 	-k https://nakadi-staging.aruha-test.zalan.do/event-types/shop-updater.article.ad21/events?batch_limit=1

# live

curl \
	-v \
	-H "Authorization: Bearer $TOKEN" \
	-H 'Content-Type: application/json' \
	-k https://nakadi-live.aruha.zalan.do/event-types/inventory-offering.AVAILABLE_QUANTITIES_CHANGED.V1/events?batch_limit=1

