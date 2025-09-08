# Project 3 Prep

**For tessellating hexagons, one of the hardest parts is figuring out where to place each hexagon/how to easily place hexagons on screen in an algorithmic way.
After looking at your own implementation, consider the implementation provided near the end of the lab.
How did your implementation differ from the given one? What lessons can be learned from it?**

Answer:My implementation mainly focused on building a single hexagon row by row, then calculating neighbor positions (left/right and above) manually with helper methods. The given implementation instead derived a more formulaic placement system, often using simple math with offsets to determine where the next hexagon belongs.
The main lesson is that thinking in terms of patterns and symmetry (e.g., the “honeycomb” structure) simplifies the logic. Instead of handling each neighbor case separately, it’s often better to generalize with offsets or recurrence. This makes the code shorter, easier to debug, and more adaptable to larger tessellations.

-----

**Can you think of an analogy between the process of tessellating hexagons and randomly generating a world using rooms and hallways?
What is the hexagon and what is the tesselation on the Project 3 side?**

Answer:In tessellation, the hexagon is the basic building block, and the tessellation is how those blocks fit together to cover space.
In Project 3, the room is the basic building block, and the random world generation is how those rooms (and hallways) are placed and connected together to form a navigable map. Just like tessellation ensures coverage without gaps, world generation ensures the map is connected and playable.

-----
**If you were to start working on world generation, what kind of method would you think of writing first? 
Think back to the lab and the process used to eventually get to tessellating hexagons.**

Answer:I would start with a method to create a single basic unit (like drawing one room at a given coordinate). This is similar to first learning how to draw a single hexagon. Once that works, I would add a way to place multiple rooms with specific offsets or constraints. Then I would focus on connectivity (like hallways) to ensure rooms are not isolated. The lesson from the hexagon lab is: start small with one shape, then build outward with systematic placement rules.

-----
**What distinguishes a hallway from a room? How are they similar?**

Answer:A hallway is typically narrow and long, serving as a connector between rooms. A room is wider, more open, and usually a destination.
They are similar in that both are rectangular areas carved into the world grid and bounded by walls. Both follow the same “fill with floor tiles and surround with walls” logic. The difference is mainly in proportions (long vs. wide) and purpose (connector vs. destination).
