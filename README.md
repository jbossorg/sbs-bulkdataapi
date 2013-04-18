Jive SBS plugin - bulk data API
===============

Jive SBS plugin providing bulk data REST API

REST API
--------

/data-api/content.jspa?updatedAfter&maxSize&spaceId&type
Parameters:

* updatedAfter - Java timestamp in millilis. All content updated after this timestamp is returned.
* maxSize - number maximum how many items to return.
* spaceId - internal Jive SBS space Id
* type - either `document` or `forum`

Returns following JSON:

		{
			id        : "SBS internal Id",
			title     : "Content title",
			content   : "XHTML Content",
			tags      : [""],
			published : timestamp,
			updated   : timestamp,
			author    : {
				email     : "author's e-mail",
				full_name : "first and last name"
			},
			url       : "URL of original content",
			comments  : {[
				{
					content   : "",
					author    : {
						email     : "",
						full_name : ""
					},
					published : timestamp
				}
			]}
		}

