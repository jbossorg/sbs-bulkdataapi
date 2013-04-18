Jive SBS plugin - bulk data API
===============

Jive SBS plugin providing bulk data access REST API useful for fulltext search
indexing. Is designed to cooperate with 
[Universal remote system indexing River Plugin for ElasticSearch](https://github.com/jbossorg/elasticsearch-river-remote)

REST API
--------

		/data-api/content.jspa?updatedAfter&maxSize&spaceId&type

Parameters:

* updatedAfter - optional, Java timestamp in millis. All content updated after this timestamp is returned.
* maxSize - optional, how many items to return maximally (defaults to 20, max 100).
* spaceId - mandatory, internal Jive SBS space Id to get content for
* type - mandatory, either `document` or `forum`

Notes:
* Returned `items` are ordered by 'last update timestamp' (value stored in `updated` field) ascending to allow incremental updates.
* Forum Question is returned as item data, all replies as comments for `forum` type.
* `updated` field for item contains timestamp of last comment published!

Returns following JSON:

		{
			"items" : [
		 		{
					"id"        : "SBS unique internal Id",
					"title"     : "Content title",
					"content"   : "XHTML Content",
					"tags"      : [""],
					"published" : timestamp,
					"updated"   : timestamp,
					"author"    : {
						"email"     : "author's e-mail",
						"full_name" : "first and last name"
					},
					"url"       : "URL of original content",
					"comments"  : [
						{
							"content"   : "XHTML Content",
							"author"    : {
								"email"     : "",
								"full_name" : ""
							},
							"published" : timestamp
						}
					]
				}
			]
		}

