# Login check flow

``` mermaid
flowchart TD

    callerApp((Calling app))
    alert(((Alert screen)))
    login(((Attempt login)))

    rootCheck{"Is device\nrooted"}
    intentMapper[Map Intent to action]
    isSameProjectId{"Is same\nproject ID"}
    isSignedIn{"Is user\nsigned in"}
    handleLogin{"Was login\nsuccessfull"}
    loginError{"Was login\nerror"}
    isProjectActive["Check project\nstate"]

    proceedAction(((Orchestrate\nthe action)))
    appResponse((Return response))

    %% Success path
    callerApp --"Intent\n(action + extras)"--> rootCheck
    rootCheck --"Not rooted"--> intentMapper
    intentMapper --> isSameProjectId
    isSameProjectId -- "Same project ID"--> isSignedIn
    isSignedIn --"Signed in"--> isProjectActive
    isProjectActive --"Active"--> proceedAction
    proceedAction --> appResponse
    
    linkStyle 0,1,2,3,4,5,6 stroke:green;

    %% Login sub-flow
    isSignedIn --"Not signed in"--> login
    isProjectActive --"Ended or\nCompromised"--> login
    login --> handleLogin
    handleLogin --"Success"--> isProjectActive
    
    linkStyle 7,8,9,10 stroke:yellow;

    %% Failure paths
    rootCheck -- "Device rooted"--> alert
    intentMapper --"Invalid intent"--> alert
    isSameProjectId --"Mismatched project ID"--> alert
    handleLogin --"Failure"--> loginError
    loginError --"Cancelled"--> appResponse
    loginError --"Error"--> alert
    isProjectActive --"Paused or Ending"--> alert
    alert --> appResponse

    linkStyle 11,12,13,14,15,16,17,18 stroke:red;
```
