Jive SBS plugin - bulk data API
===============================

Jive SBS plugin providing bulk data access REST API useful for fulltext search
indexing. Is designed to cooperate with 
[Universal remote system indexing River Plugin for ElasticSearch](https://github.com/jbossorg/elasticsearch-river-remote)

REST API
--------

####List identifiers for all spaces

	/data-api/spaces.jspa

Returns following JSON:

	{
		"spaces" : ["12542","4525455","2565"]
	}		
		

####Get data for space

	/data-api/content.jspa?updatedAfter&maxSize&spaceId&type

Parameters:

* spaceId - mandatory, internal Jive SBS space Id to get content for
* type - mandatory, either `document` or `forum`
* updatedAfter - optional, Java timestamp in millis. All content updated after this timestamp is returned.
* maxSize - optional, how many items to return maximally (defaults to 20, max 100).

Notes:

* Returned `items` are ordered by 'last update timestamp' (value stored in `updated` field) ascending to allow incremental updates.
* Forum thread first message is returned as item data, all replies as comments for `forum` type.
* `updated` field for item contains timestamp of last comment published!
* all dates in response (`published`,`updated`) are represented as strings containing Java timestamp in millis (millis from 1.1.1970 UTC) 

Returns following JSON:

	{
		"items" : [
	 		{
				"id"        : "SBS unique internal Id",
				"title"     : "Content title",
				"content"   : "XHTML Content",
				"tags"      : ["tag1","tag2"],
				"published" : "timestamp",
				"updated"   : "timestamp",
				"url"       : "URL of original content",
				"authors"   : [ 
					{
						"email"     : "author's e-mail",
						"full_name" : "first and last name"
					}
				],
				"comments"  : [
					{
						"content"   : "XHTML Content",
						"authors"    : [
							{
								"email"     : "",
								"full_name" : ""
							}
						],
						"published" : "timestamp"
					}
				]
			}
		]
	}

