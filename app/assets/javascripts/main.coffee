class @Wally
  constructor: (@wsUrl) ->
    @drawMap()
    @connect()

  drawMap: ->
    color = d3.scale.category10()

    # kavrayskiy7, equirectangular, stereographic, winkel3
    @projection = d3.geo.winkel3().scale(200).translate([500,300])
    path = d3.geo.path().projection(@projection)

    @svg = d3.select("#map").append("svg")
      .attr("width", 1440)
      .attr("height", 750)

    d3.json "/assets/data/world-110m.json", (error, world) =>
      countries = topojson.object(world, world.objects.countries).geometries

      @svg.selectAll(".country")
        .data(countries)
        .enter().insert("path")
        .attr("class", "country")
        .attr("d", path)

      @svg.insert("path")
        .datum(topojson.mesh(world, world.objects.countries, (a, b) -> a.id isnt b.id ))
        .attr("class", "country-boundary")
        .attr("d", path)

  plot: (points) ->
    plottedPoints = @svg.selectAll('.point')
      .data(points)
      .enter().insert('circle')
      .attr("class", 'point')
      .attr("cx", (d) => @projection([d.longitude, d.latitude])[0])
      .attr("cy", (d) => @projection([d.longitude, d.latitude])[1])
      .attr("r", 0)
      .transition()
      .attr("r", 20)
      .duration(1000)
      .transition()
      .attr("r", 4)

  connect: ->
    @ws = new WebSocket(@wsUrl)
    @ws.onmessage = (event) =>
      console.log '*** message:', event
