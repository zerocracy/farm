# Architecture overview

## Schema
![schema](/doc/zerocracy-arc.png)

## Workflow
Application listen for webhooks in `TkGithub` and `TkGitlab`
classes and handles it in `Reaction` implementations.
Also it's connected to Telegram and Slack chats and listen for
user messages here.
Both webhook reactions and chat-bot reactions uses `Question` class
to transform request to a "claim".

Each claim is submitted to project claim-queue (project item `claim.xml`)
and processed by `Flush` implementations which takes all claims of a project
from `claims.xml` and start the `Brigade` of `Stakeholder`s to process it.

`Stakeholder`s implemented as groovy scripts with procedural code inside.
Each script can handle one or more types of claim with help of `Assume` class.
Stakeholder can submit new claims which will be processed on next `Flush`.
Also it can interruct with a user via GitHub/GitLab API or Telegram/Slack chat bots.
