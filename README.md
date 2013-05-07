 A simple programmatically configured load tester with standardised graph reporting. Note that this framework is built with CI or development load testing in mind. It is not a serious implementation for capacity planning as it cannot manage multiple injectors at present. To use, either:

- implement a PayloadGenerator that gets called with every request to generate a new payload to hit an endpoint with,
- implement a PerUserPayloadGenerator that gets called for every request but with a unique user id to run that request against
- implement a TestGraphWalkerBuilder (and corresponding TestNode implementations) and use the PayloadGraphAdapter to produce a load test that operates over a state machine, sending a sequence of request types one after the other.
