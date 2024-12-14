*I wrote this report on a talk at the 2015 ACCU conference as a gist on May 12th 2015. Reusing it as a blog post in a desperate attempt to produce more content.*

# Notes from Michael Feather's talk "Organisational Machinery around Software"

Michael Feathers gave a thought provoking [talk in Bristol](http://www.meetup.com/Bath-Scrum-User-Group/events/221397746/) called "Organisational Machinery around Software". He gave this talk to the Bath Scrum Users Group, on the eve of the [2015 ACCU conference](http://accu.org/index.php/conferences/accu_conference_2015).

He talked about the idea of placing code and architecture first, making team structure, process and feature choice subservient to it. He suggested that this heretical perspective could help us understand better the range of options open to us in development.

Software development processes typically have little to say about the quality of code or architecture. The project and organisational decisions don't have an immediate effect on the code or architecture but will often have significant later consequences for them. From a system perspective what are people not talking about or not paying attention to? What are the system constraints on code quality and architecture? Could there be a view of the code assets across the organisation rather than it being a strictly technical concern.

## Examples of how processes and ways of organising teams can impact the quality of the code:

### Component misalignment

This can come about through a failure of the teams developing the different components to communicate effectively.

### Check-in gates

Check-in gates allow code to be integrated only if it passes certain automated tests. Tests could be for style and coverage. The feedback loop to the author could be long and so could lead to a more conservative style, such as doing less refactoring. (Apparently at one time Microsoft used check-in gates with feedback loops of over 1 day)

### Interface cruft

Once agreed there is a reluctance to change an interface. This can lead to the implementation using the interface in tortured ways rather then the interface being refactored. (My example - many calls to a method in an interface resulting in chatty use of a network rather than one call to an aggregate method.)

### Unintentional boundary

Boundaries or interfaces can form in the code that reflect the team's organisational structure even if there is no desire for a corresponding boundary in the software. This is an example of Conways law.

### Staff turnover

People leaving a team can result in the remaining members having a lack of knowledge of why decisions were made, or what conventions have been followed.

When this slide was shown: "Legacy happens when code turns over at lesser rate than personnel" Rob Smallshire heckled with some data from his own research, in his organisation half of developers move on every 3.5 years, half of code is replaced every 35 years. [corresponding tweet](https://twitter.com/robsmallshire/status/590585936267837442). See his [blog post](http://sixty-north.com/blog/predictive-models-of-development-teams-and-the-systems-they-build) for more info.

## Practices

Can we flip our perspective and make code quality a primary consideration? Given that these effects are inevitable can we adopt practices that mitigate or align with them?

### Informed Feature Selection

Can we make the business stakeholders aware of the quality of code and architecture so they can understand how that might impact the delivery of future work? Choosing to avoid, delete or visit areas of the code base with poor quality in order to reduce the carrying cost or prepare for features in that area.

#### Can we learn from the military viewpoint?

Apparently the military consider the quality of their assets when planning their moves on the battlefield. What will be the attrition rate? How good are these soldiers? Is that objective achievable with the number of tanks we have?

### Personnel rotation

Who is good at what? Should we assign people to tasks based on their talents. Gelled teams vs dynamic re-teaming. Feathers posited that, like consultants, developers can become good at parachuting into a team and becoming effective quickly.

### Planned obsolescence

Should software have a fixed lifespan? Should code beyond a certain age be thrown away and rewritten. How would we develop differently with this in mind?

### Tending

He suggested a gardening metaphor is a useful way of considering the effects of time on software and how we might need to continually weed/tend code to be able to continue to work with it in the future. Can we assess the quality of our assets and perform work to directly address that? Feathers said that he preferred qualitative to quantitive metrics.

## Summary

Feathers said he wanted the talk to provoke thinking on this topic and that he was experimenting with some of the practices with some of his consulting clients.