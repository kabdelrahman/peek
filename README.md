# Peek

<i><b>peek</b> <tt>/piːk/</tt>(verb) &mdash; look quickly or furtively</i>

peek is a command line tool to inspect [Nakadi](https://github.com/zalando/nakadi) streams &mdash; sort of like `tail -f` ...

* [Usage](#usage)
* [Examples](#examples)
* [Installation](#installation)
* [Troubleshooting](#troubleshooting)
* [Todo](#todo)
* [License](#license)

## Usage

	$ peek --help
	peek [options] [event_type] [patterns]

	  -a, --array                     return array of values
	  -h, --host                      use nakadi host [name] from config
	  -n, --number                    exit after processing [number] of events
	  -p, --pretty                    pretty-print json output
	      --help                      display this help and exit		
`peek` fetches events from all partitions of the given event type, starting at the latest offset, and prints them to standard out. for an event type with few updates, `peek` may appear to 'hang' ... until the first event is available. see examples below for use of options and patterns.

`peek` expects to find a **valid oauth token** in the environment variable `$TOKEN`

## Examples

let's say we want to explore the nakadi event type `whisky-world.cart-service.v2`

	$ export whisky=whisky-world.cart-service.v2

we start by fetching a single event:

	$ peek -n 1 $whisky
	{"metadata":{"occurred_at":"2016-10-26T14:47:26.553Z","eid":"dc638894-582d-4546-9a9c-a1e3ccabe987","event_type":"whisky-world.cart-service.v2","partition":"2","parent_eids":["27144a9f-a408-40da-bc7d-2df8dc37e0bc"],"flow_id":"497wrQYJYVkJxaOfy0yHVYpx","received_at":"2016-10-26T14:47:26.568Z"},"data_op":"U","data":{"version":42,"customer":{"id":94371},"items":[{"brand":"Auchroisk","years":10,"price":[{"value":47.45,"unit":"GBP"},{"value":53.95,"unit":"EUR"}],"tags":["light","fresh"]},{"brand":"Dalwhinnie","years":15,"price":[{"value":36.95,"unit":"GBP"},{"value":45.75,"unit":"USD"}],"tags":["elegant","fruity","smooth"]}]}}

ok, that's really hard to read. how about some pretty printing?

	$ peek -n 1 --pretty $whisky
	{
	  "metadata": {
	    "occurred_at": "2016-10-26T14:47:26.553Z",
	    "eid": "dc638894-582d-4546-9a9c-a1e3ccabe987",
	    "event_type": "whisky-world.cart-service.v2",
	    "partition": "2",
	    "parent_eids": [
	      "27144a9f-a408-40da-bc7d-2df8dc37e0bc"
	    ],
	    "flow_id": "497wrQYJYVkJxaOfy0yHVYpx",
	    "received_at": "2016-10-26T14:47:26.568Z"
	  },
	  "data_op": "U",
	  "data": {
	    "version": 42,
	    "customer": {
	      "id": 94371
	    },
	    "items": [
	      {
	        "brand": "Auchroisk",
	        "years": 10,
	        "price": [
	          {
	            "value": 47.45,
	            "unit": "GBP"
	          },
	          {
	            "value": 53.95,
	            "unit": "EUR"
	          }
	        ],
	        "tags": [
	          "light",
	          "fresh"
	        ]
	      },
	      {
	        "brand": "Dalwhinnie",
	        "years": 15,
	        "price": [
	          {
	            "value": 36.95,
	            "unit": "GBP"
	          },
	          {
	            "value": 45.75,
	            "unit": "USD"
	          }
	        ],
	        "tags": [
	          "elegant",
	          "fruity",
	          "smooth"
	        ]
	      }
	    ]
	  }
	}

much better! so, what we're really interested in is the customer id. let's select it by providing a pattern:

	$ peek -n 1 --pretty $whisky data.customer.id
	{
		"data.customer.id": 94371
	}
	
we can also use multiple patterns, and each pattern can select an arbitrary json value (including arrays and objects):

	$ peek -n 1 --pretty $whisky data.customer.id metadata.parent_eids
	{
		"data.customer.id": 94371,
		"metadata.parent_eids": [
   			"27144a9f-a408-40da-bc7d-2df8dc37e0bc"
    	]
    }
    
we can even filter down into arrays, for example:

	$ peek -n 1 --pretty $whisky data.customer.id data.items.brand
	{
		"data.customer.id": 94371,
		"data.items.brand": [
			"Auchroisk",
			"Dalwhinnie"
		]
	}
	
this also works multiple times, like:
	
	$ peek -n 1 --pretty $whisky data.customer.id data.items.price.unit
	{
		"data.customer.id": 94371,
		"data.items.price.unit": [
			[
				"GBP",
				"EUR"
			],
			[	
				"GBP",
				"USD
			]
		]
	}


the special value `cursor` represents the cursor (= partition + offset) of the event:

	$ peek -n 1 $whisky cursor data.customer.id
	{"cursor":"2:440870215","data.customer.id":94371}

sometimes, we can make the output more readable by stripping away the keys:

	$ peek -n 1 --array $whisky cursor data.customer.id
	["2:440870215",94371]

if we now leave out the `-n 1` option, we get a continuous stream of events:

	$ peek --array $whisky cursor data.customer.id
	["2:440870215",94371]
	["1:440651127",88652]
	["2:440870216",1004]
	["0:420666420",45326]
	...


## Installation

1. make sure you have java installed:

		$ java -version
		java version "1.8.0_72"

1. clone the project into your home directory:

		$ git clone https://github.com/zalando-incubator/peek.git ~/.peek
		Cloning into ...

1. configure one or more nakadi hosts:

		$ cd ~/.peek/bin
		$ cp peek.config.sample peek.config
		$ vim peek.config

1. load the `peek` function into your shell:

		$ source ~/.peek/bin/peek.sh
	
	(add this line to your `~/.bash_profile` to make `peek` available globally.)
	
1. done!

		$ peek --help
	

## Troubleshooting

### sun.security.validator.ValidatorException: PKIX path building failed

`peek` could not establish an SSL connection. you may have to install the right CA root certificate for your nakadi server.

### java.lang.NullPointerException

the environment variable `$TOKEN` does not contain a token.

### org.zalando.straw.Straw$HttpException: 400 | 401 | 403 

the environment variable `$TOKEN` contains a token &mdash; but it is either not valid, or it has expired, or it has the wrong type, or it has the wrong scopes.


## Todo

* add option to set starting cursors
* add stats as output value
* [bartosz] add option to list cursors for event type ?
* [daniel] add option to run bisect search ?
* add option to configure sample interval ?
* add option to calculate histograms ?

## License

MIT
