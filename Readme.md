**Functional Description**:
Tavlei Game. Rules [here](http://tavlei.net/pravila/).

*Functions*:
* Include Server and Client parts. Client can be browser or JavaFX client.
* Supports composition and playing few contemporaneous game parties between different players via Server. 
* Simple AI player.


**Description and technologies**:

Uses  AOP AspectJ, Spring, JavaFX. Uses the author's [maven plugin](https://github.com/Tusenka/Maven) for generate javascript classes from Java classes. 

Uses design patterns: 
* Creational patterns: Factory method, Singleton, Prototype,
* Structural patterns: Decorator, Facade,
* Behavioral patterns: Strategy, Event Channel (Publish/subscribe).

And such patterns as Proxy in Junit tests, Iterator, template method, etc...

Examples of patterns in classes:
* Factory method - GamePartyModel, EventManager classes,
* Singleton with lazy initialization - JavaTavleiClientController class,
* Prototype- ControllerManager class,
* Decorator - EventManager and EventManager.EventManagerMock classes,
* Facade - ControllerManager class,
* Strategy - TavleiBoardImpl class,
* Event Channel - EventManager class.


Game components perform interaction by messages (EventManager class).
Interaction between game components perform by messages (EventManager class).

Server allows selection and composition parties. At the same time there can exists few parties between different players, include browser clients and/or JavaFx clients.

Browser part was created from other [abandoned project](http://tavlei.net/pravila/) for presentation Java сервера.

Game is created based on [JavaFX chess с GitHub](https://github.com/rgolding/cs1331HW/tree/master/HW5%20copy/hw-chess-master).
JavaFx client is fat.

Example of UML 2 sequence diagram for Game Party [here](http://creately.com/diagram/j3uc19ok2/lPFFFxRSDGHGIgzNcsVcMks%3D).


