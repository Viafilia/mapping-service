# Mapping-Service
The project was created as an assignment for oop2 course

# Functionality

## Request `Get /amenities`

**Parameters** 

- `amenity`: optional, can specify the type of amenity to return, if empty: returns all types
- `bbox.tl.x`, `bbox.tl.y`: top-left of the bounding box to search for
- `bbox.br.x`, `bbox.br.y`: bottom-right of the bounding box to search for
- `point.x`, `point.y`: center of the point  
- `point.d`: maximum distance to the point, in meters 
- `take` (default: 50) optional parameter for paging
- `skip` (default: 0): optional parameter for paging, skip is how many you need to step over.

## Request `Get /amenities/{id}`

This request should return all information about a single point of interest based on its ID.


## Request `Get /roads`

**Parameters:**

- `road`: optional, can specify the type of road to return, if empty: returns all types
- `bbox.tl.x`, `bbox.tl.y`: top-left of the bounding box to search for 
- `bbox.br.x`, `bbox.br.y`: bottom-right of the bounding box to search for 
- `take` (default: 50) optional parameter for paging
- `skip` (default: 0): optional parameter for paging, skip is how many you need to step over. 

## Request `Get /roads/{id}`

This request should return all information about a single point of interest based on its ID.

## Request: `GET /tile/{z}/{x}/{y}`

**Parameters:**

-  `layers` (default: `motorway`): A comma-separated list of layers to display. Layers should be drawn in the order specified, with the last one being drawn last.


Renders map tiles as PNG files that will be displayed on a map. The output images are of size `512 x 512` pixels in PNG format.

## Request: `GET /route`

**Parameters:**

-  `from`: start node ID
-  `to`: end node ID
-  `weighting`: either `time` or `length` (default: `length`)

Finds a shortest path from given start point to the end point, prioritizing eather time or length

## Request: `GET /usage`

**Parameters:**

- `bbox.tl.x`, `bbox.tl.y`: Top-left coordinates of the bounding box to analyze.
- `bbox.br.x`, `bbox.br.y`: Bottom-right coordinates of the bounding box.

Calculates and return the percentage and area occupied by different land usage types within a specified bounding box.

