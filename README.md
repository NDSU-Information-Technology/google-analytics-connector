<!--
Copyright (c) 2026 North Dakota State University

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->

# Google Analytics connector for ConnID and midPoint

## Overview

Connector for Google Analytics using ConnId. This is provided as-is.

Copyright 2026 North Dakota State University and released under the Apache License, Version 2.0.

Used by NDSU to manage Qualtrics. For support see a midPoint support organization like Unicon. Pull requests welcome.

Deployed on midPoint 4.10. 

## Assumptions

This is setup to operate in the way required by NDSU. If that doesn't work for your institution, pull requests to add functionality are welcome. 

Only manages users, not roles. Works against a property not an account.

# Setup

To create authorization, follow:

https://developers.google.com/analytics/devguides/config/admin/v1/quickstart-client-libraries

## Configuration

| Key | Value |
| ---- | ----- |
| property | Property being managed, but its numeric id |
| jsonPath | Path to json file with creds |
| directRole | Role to assign, such as predefinedRoles/analyst |

The role can be later changed directly in Google Analytics if the person needs something different.


## Schema

| Attribute | Type | Comment |
| -----     | ---- | ------- |
| id | String | _ _ UID _ _ |
| email | STring | _ _ NAME _ _ |
