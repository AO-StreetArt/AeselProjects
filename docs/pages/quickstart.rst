.. _quickstart:

Getting Started with AeselProjects
==================================

:ref:`Go Home <index>`

Docker
------

An official Docker Image of AeselProjects is provided, and to get you up and running
quickly, a Docker Compose file is provided as well.  To start up a Mongo
instance, a Consul instance, and a AeselProjects instance, simply run the following
from the 'compose/min' folder:

.. code-block:: bash

   docker-compose up

Once the services have started, test them by hitting AeselProjects' healthcheck endpoint:

.. code-block:: bash

   curl http://localhost:5644/health

Keep in mind that this is not a secure deployment,
but is suitable for exploring the :ref:`AeselProjects API <api>`.

Building from Source
--------------------

Once you've got the required backend services started, build and execute the tests
for the repository.

``./gradlew check``

And, finally, start AeselProjects:

``./gradlew bootRun``

Using the Latest Release
------------------------

AeselProjects can also be downloaded as a runnable JAR for the latest release from `here <https://github.com/AO-StreetArt/AeselProjects/releases>`__.

When using a JAR, unzip the downloaded package, move to the main directory from a terminal, and run:

``java -jar build/libs/aeselprojects-0.0.1.jar``
