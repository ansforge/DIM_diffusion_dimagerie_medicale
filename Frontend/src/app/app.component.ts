/*
 *  app.component.ts - DRIMBox
 *  Copyright 2022 b<>com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CookieService } from 'ngx-cookie-service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})

export class AppComponent {
  title = 'ApiDrim';

  // boolean indicates if user is connected
  connected = false;
  // ins retrieved in url parameter
  ins: string;
  // auth date of user
  dateAuth: string;
  // name + surname of user
  authenticator: string;

  /**
   * Constructor : manage local datas, session token and check if user connected
   * @param http for rest requests
   * @param cookieService to manage user cookies
   * @param route to manage angular routes
   */
  constructor(private readonly http: HttpClient, private readonly cookieService: CookieService, private readonly route: ActivatedRoute) {
    this.authenticator = "DOCTEUR RADI";
    this.dateAuth = "04/10/2022 15:42";

    this.route.queryParams.subscribe(params => {
      // Retrieve url parameter (ins)
      this.ins = params['ins'];
      if (this.ins !== undefined) {
        this.connected = true;
      }
      // Generate sessionToken
      if (!cookieService.check("SessionToken")) {
        const uuid = this.generateUUID();
        this.cookieService.set("SessionToken", uuid);
      }
      this.verifyConnection();
    });
  }

  /**
   * Verify with backend if user connected
   * */
  verifyConnection() {
    this.http.get('/api/auth', { responseType: 'text' }).subscribe(data => {
      // Back can answer : connected -- means user is already connected
      //                   connected but no structure : + list structs -- means user is already connected but activity struct no selected, gives list of user structs
      //                   no connected : + url -- means user is not connected, gives url to ProSanteConnect
      if (data.startsWith("no connected : ")) {
        // No connected, redirect to ProSanteconnect window
        const url = data.split(": ")[1];
        window.location.replace(url);
      }
      else if (data.startsWith("connected but no structure : ")) {
        window.location.replace("https://localhost/ris");
      }
    });
  }

  /**
   * Function to generate UUID
   * */
  generateUUID() { // Public Domain/MIT
    let d = new Date().getTime();//Timestamp
    let d2 = ((typeof performance !== 'undefined') && performance.now && (performance.now() * 1000)) || 0;//Time in microseconds since page-load or 0 if unsupported
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
      let r = Math.random() * 16;//random number between 0 and 16
      if (d > 0) {//Use timestamp until depleted
        r = (d + r) % 16 | 0;
        d = Math.floor(d / 16);
      } else {//Use microseconds since page-load if supported
        r = (d2 + r) % 16 | 0;
        d2 = Math.floor(d2 / 16);
      }
      return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
    });
  }
}
