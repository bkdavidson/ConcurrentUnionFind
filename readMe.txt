Dependencies to build and run: java 7, ant
To run first use these ant commands: 'ant compile jar'
To execute type in: 'java -jar ./build/jar/connectedCities.jar input.txt cityA cityB'
	where input.txt is your desired input file, and the two cities are the cities to test for connectivity.

All command line parameters in ordinal position are:
	1- input files, required
	2- first city to test (starting point), required
	3- second city to test (end point), required
	4- Integer for number of threads, optional
	5- 'true' (caps insensitive) if you want timing data displayed, optional
	
You may need to use -Xmx option on java to get a larger heap for very big input files with a huge unique number of cities. About 500,000 unique cities would fit into 150 MB heap.


