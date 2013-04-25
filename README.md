Jive SBS plugin - Bulk data API
===============================

Jive SBS plugin providing bulk data access REST API useful for fulltext search
indexing. Is designed to cooperate with 
[Universal remote system indexing River Plugin for ElasticSearch](https://github.com/jbossorg/elasticsearch-river-remote)

REST API operations
-------------------

####List identifiers for all spaces

	/data-api/spaces.jspa

Returns following JSON:

	{
		"spaces" : ["12542","4525455","2565"]
	}		
		

####Get data for space

	/data-api/content.jspa?spaceId&type&updatedAfter&maxSize

Parameters:

* `spaceId` - mandatory, internal Jive SBS space Id to get content for
* `type` - mandatory, type of SBS content to return, either `document` or `forum`
* `updatedAfter` - optional, Java timestamp in millis. Content updated after this timestamp is returned only so you can use incremental updates.
* `maxSize` - optional, how many items is returned maximally (defaults to 20, max 100).

Notes:

* Returned `items` are ordered by 'last update timestamp' (value stored in `updated` field) ascending to allow incremental updates.
* for `forum` type - forum thread first message is returned as item data, all replies as comments.
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

REST API authentication
-----------------------

Client must be authenticated to use REST API of this plugin. You can use 
HTTP Basic Authentication with standard Jive SBS user credentials.  
If `Bulk Data API Users` group is defined in SBS, then only users present 
in this group can use this API. If group doesn't exists, then all authenticated
users can use it.  