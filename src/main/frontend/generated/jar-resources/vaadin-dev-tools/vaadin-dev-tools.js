import{LitElement as D,css as j,html as z}from"lit";import{property as V,query as W,state as q,customElement as Z}from"lit/decorators.js";function b(o,e,t,s){var r=arguments.length,n=r<3?e:s===null?s=Object.getOwnPropertyDescriptor(e,t):s,a;if(typeof Reflect=="object"&&typeof Reflect.decorate=="function")n=Reflect.decorate(o,e,t,s);else for(var l=o.length-1;l>=0;l--)(a=o[l])&&(n=(r<3?a(n):r>3?a(e,t,n):a(e,t))||n);return r>3&&n&&Object.defineProperty(e,t,n),n}class K extends HTMLElement{#o;#n;#e;#s;#t;constructor(){super(),this.#o=null,this.#e=!1,this.#s=null,this.#t=null,this.#n=this.attachShadow({mode:"closed"}),this.render(),this.setupProtection()}static get observedAttributes(){return["expired","start-failure","license-download"]}render(){const e=`
      <style>
        :host {
          position: fixed;
          bottom: 0;
          left: 0;
          right: 0;
          z-index: 9999;
          min-height: 100% !important;
          min-width: 100% !important;
          display: flex !important;
          visibility: visible !important;
          opacity: 1 !important;
          clip-path: none !important;
          text-indent: 0 !important;          
          background-color: rgba(0, 0, 0, 0.5);          
        }
        
        .container {
          background: white;
          border-radius: 0.5rem;
          box-sizing: border-box;
          color: #3f4d62;
          font-family: "nb_international_pro","ui-sans-serif","system-ui","-apple-system","BlinkMacSystemFont","Segoe UI","Roboto","Helvetica Neue","Arial","Noto Sans","sans-serif","Apple Color Emoji","Segoe UI Emoji","Segoe UI Symbol","Noto Color Emoji";
          font-size: 0.875rem;
          font-weight: normal;
          letter-spacing: 0.01em;
          line-height: 1.7;
          margin: auto;
          max-width: 32rem;
          padding: 1.5rem 1.5rem 1rem 1.5rem;
        }
        
        h2 {
          color: #0d1219;
          font-size: 1.5rem;
          line-height: 1.2;
          margin: 0 0 1rem 0;
        }
        
        p {
          margin: 0;
        }
        
        span.badge {
          background: #F1F5FB;
          border-radius: 4px;
          display: inline-block;
          font-size: 0.8125rem;
          font-weight: 600;
          line-height: 1.7;
          padding: 0 6px 0 4px;
        }
        
        span.badge svg {
          vertical-align: sub;
        }
        
        p:has(+ ul) {
          color: #0d1219;
          font-weight: 600;
          margin-top: 1.25rem;
        }
        
        ul {
          display: flex;
          flex-direction: column;
          gap: 0.5rem;
          list-style: none;
          margin: 0.5rem 0 1.5rem 0;
          padding: 0;
        }
        
        ul li {
          align-items: center;
          display: flex;
          gap: 0.25rem;
        }
        
        ul li span:first-of-type {
          color: #0d1219;
          font-weight: 600;
        }
        
        hr {
          border-color: rgb(224, 233, 244);
          border-top: 0;
          margin: 1.5rem 0 0.75rem 0;
        }
        
        button {
          align-items: center;
          background: #F1F5FB;
          border: none;
          border-radius: 8px;
          display: flex;
          color: #0368DE;
          font-family: "nb_international_promono","ui-monospace","SFMono-Regular","Menlo","Monaco","Consolas","Liberation Mono","Courier New","monospace";
          font-size: inherit;
          font-weight: 600;
          height: 2.375rem;
          justify-content: center;
          line-height: 1.7;
          padding: 0;
          width: 100%;
        }
        
        button.primary {
          background: #0368DE;
          color: white;
          flex-direction: column;
          height: 4.5rem;
        }
        
        button.primary span + span {
          font-size: 0.8125rem;
          font-weight: normal;
        }
        
        button.primary + button.secondary {
          margin-top: 0.5rem;
        }
        
        hr + p {
          font-size: 0.8125rem;
          line-height: 1.7;
          text-align: center;
        }
        
        a {
          color: #0368DE;
        }
        
        .error {
          background: #ffedee;
          border-radius: 0.75rem;
          display: flex;
          flex-direction: column;
          gap: 0.25rem;
          margin-top: 1.5rem;
          padding: 1rem 1.5rem;
        }
        
        .error h3 {
          color: #0d1219;
          font-size: inherit;
          line-height: inherit;
          margin: 0;
        }
        
        .error a {
          font-weight: 600;
        }
      </style>
     `;this.#n.innerHTML=`
    ${e}
    <div class='container'>
      ${this.#e?`
        <h2>Trial expired</h2>
        <p>
          Vaadin Core is free and open-source. Sign in to keep using
          <span class="badge">
            <svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
<path d="M12 15V9.75H15V15H12ZM7.5 15V3H10.5V15H7.5ZM3 15V6.75H6V15H3Z" fill="url(#paint0_linear_85_186)"/>
              <defs>
                <linearGradient id="paint0_linear_85_186" x1="9" y1="3" x2="9" y2="15" gradientUnits="userSpaceOnUse">
                  <stop stop-color="#1A81FA"/>
                  <stop offset="1" stop-color="#8854FC"/>
                </linearGradient>
              </defs>
            </svg>
            Pro components
          </span> and 
          <span class="badge">
            <svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M8.2125 11.6625L12.45 7.425L11.3812 6.35625L8.2125 9.525L6.6375 7.95L5.56875 9.01875L8.2125 11.6625ZM9 16.5C7.2625 16.0625 5.82812 15.0656 4.69687 13.5094C3.56562 11.9531 3 10.225 3 8.325V3.75L9 1.5L15 3.75V8.325C15 10.225 14.4344 11.9531 13.3031 13.5094C12.1719 15.0656 10.7375 16.0625 9 16.5ZM9 14.925C10.3 14.5125 11.375 13.6875 12.225 12.45C13.075 11.2125 13.5 9.8375 13.5 8.325V4.78125L9 3.09375L4.5 4.78125V8.325C4.5 9.8375 4.925 11.2125 5.775 12.45C6.625 13.6875 7.7 14.5125 9 14.925Z" fill="url(#paint0_linear_85_190)"/>
              <defs>
                <linearGradient id="paint0_linear_85_190" x1="9" y1="1.5" x2="9" y2="16.5" gradientUnits="userSpaceOnUse">
                  <stop stop-color="#1A81FA"/>
                  <stop offset="1" stop-color="#8854FC"/>
                </linearGradient>
              </defs>
            </svg>
            Team features
          </span> for 30 more days.
        </p>
        <p>Continue getting full access to:</p>
        ${this.getProductsList()}
        <button ${this.#t==="started"?"disabled":""} class='primary'>
          <span>Extend trial 30 days</span>
          <span>Sign up ⋅ No credit card required</span>
        </button>
        `:`
        <h2>Get full access to all features</h2>
        <p>
          Vaadin Core is free and open-source. To use Pro components like <span class="badge">
          <svg width="18" height="18" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M12 15V9.75h3V15h-3Zm-4.5 0V3h3v12h-3ZM3 15V6.75h3V15H3Z" fill="url(#a)"/>
          <defs>
            <linearGradient id="a" x1="9" y1="3" x2="9" y2="15" gradientUnits="userSpaceOnUse">
              <stop stop-color="#1A81FA"/>
              <stop offset="1" stop-color="#8854FC"/>
            </linearGradient>
          </defs>
        </svg>
          Charts</span> in your app, activate a free trial.
        </p>
        <p>Get full access:</p>
        ${this.getProductsList()}
        <button ${this.#t==="started"?"disabled":""} class='primary'>
          <span>Start 7-day trial</span>
          <span>No registration or credit card required</span>
        </button>
        <button ${this.#t==="started"?"disabled":""} class='secondary'>
          Activate your license
        </button>
        `}
      ${this.#s?`
        <div class='error'>
          <h3>Trial failed to start</h3>
          <p>Something went wrong while starting your trial. Try again in a moment. If the issue persists, <a href="https://pages.vaadin.com/contact" target="_blank">contact our support team</a>.</p>
        </div>`:""}
      ${this.#t==="started"?"<p><strong>Waiting for the license key to be downloaded...</strong></p>":""}
      ${this.#t==="failed"?'<div class="error">Failed to download the license key. Please try again later.</div>':""}
      <hr>
      <p>
        By starting your trial, you agree to our <a href='https://vaadin.com/commercial-license-and-service-terms' target='_blank'>terms and conditions</a>.
      </p>
    </div>
    `,this.#n.querySelector("button.primary")?.addEventListener("click",()=>{this.dispatchEvent(new CustomEvent("primary-button-click",{detail:{expired:this.#e}}))}),this.#n.querySelector("button.secondary")?.addEventListener("click",()=>{this.dispatchEvent(new CustomEvent("secondary-button-click"))})}getProductsList(){return`
        <ul>
          <li>
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="none">
            <path
              d="M16 20v-7h4v7h-4Zm-6 0V4h4v16h-4Zm-6 0V9h4v11H4Z"
              fill="url(#a)"
            />
            <defs>
              <linearGradient
                id="a"
                x1="12"
                y1="4"
                x2="12"
                y2="20"
                gradientUnits="userSpaceOnUse"
              >
                <stop stop-color="#1A81FA" />
                <stop offset="1" stop-color="#8854FC" />
              </linearGradient>
            </defs>
          </svg>
            <span>Pro components</span><span>⋅</span><span>Charts, Grid Pro, CRUD and more</span>
          </li>
          <li>
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="none">
            <path
              d="M10.95 15.55 16.6 9.9l-1.425-1.425L10.95 12.7l-2.1-2.1-1.425 1.425 3.525 3.525ZM12 22c-2.317-.583-4.23-1.913-5.737-3.988C4.754 15.938 4 13.633 4 11.1V5l8-3 8 3v6.1c0 2.533-.754 4.838-2.262 6.912C16.229 20.087 14.317 21.418 12 22Zm0-2.1c1.733-.55 3.167-1.65 4.3-3.3s1.7-3.483 1.7-5.5V6.375l-6-2.25-6 2.25V11.1c0 2.017.567 3.85 1.7 5.5s2.567 2.75 4.3 3.3Z"
              fill="url(#a)"
            />
            <defs>
              <linearGradient
                id="a"
                x1="12"
                y1="2"
                x2="12"
                y2="22"
                gradientUnits="userSpaceOnUse"
              >
                <stop stop-color="#1A81FA" />
                <stop offset="1" stop-color="#8854FC" />
              </linearGradient>
            </defs></svg
          >
            <span>Team features</span><span>⋅</span><span>Acceleration Kits</span>
          </li>
        </ul>
    `}connectedCallback(){this.setupParentRemovalProtection()}disconnectedCallback(){this.cleanup()}attributeChangedCallback(e,t,s){e==="expired"?this.handleExpiredChange(s!==null&&s!=="false"):e==="start-failure"?this.handleStartFailed(s==="expired"):e==="license-download"&&this.handleLicenseDownload(s)}handleLicenseDownload(e){this.#t!==e&&(this.#t=e,this.render())}handleExpiredChange(e){this.#e!==e&&(this.#e=e,this.render())}handleStartFailed(e){(this.#s!==e||this.#e!==e)&&(this.#e=e,this.#s=!0,this.render())}setupProtection(){const e=Element.prototype.remove;this.remove=function(){console.debug("Attempt to remove vaadin-pretrial detected - restoring");const t=this.parentNode;e.apply(this,arguments),this.restoreSplashScreen(t)},this.protectStyles()}setupParentRemovalProtection(){!this.#o&&this.parentNode&&(this.#o=new MutationObserver(e=>{e.forEach(t=>{t.type==="childList"&&t.removedNodes.forEach(s=>{s===this&&(console.debug("vaadin-pretrial removal detected - restoring"),this.restoreSplashScreen(t.target))})})}),this.#o.observe(this.parentNode,{childList:!0,subtree:!0}))}protectStyles(){Object.defineProperty(this,"style",{get(){return{}},set(e){}})}cleanup(){this.#o&&this.#o.disconnect()}restoreSplashScreen(e){e&&setTimeout(()=>{console.debug("Re-adding vaadin-pretrial component");const t=this.querySelector('[slot="products"]');e.contains(this)&&e.removeChild(this);const s=document.createElement("vaadin-pretrial");this.#e&&s.setAttribute("expired","true"),this.#s&&s.setAttribute("start-failure",this.#e?"expired":""),t&&s.appendChild(t.cloneNode(!0)),e.appendChild(s)},0)}}customElements.define("vaadin-pretrial",K);function Y(o){o.detail.expired?U():oe()}function J(){U()}const X=(o,e)=>{if(o&&!o.querySelector("vaadin-pretrial")){const t=e.preTrial?.trialState==="EXPIRED",s=document.createElement("vaadin-pretrial");t&&s.setAttribute("expired","");const r=document.createElement("div");r.setAttribute("slot","products"),r.innerHTML=`
      This application is using:
      <ul>
        <li>${e.product.name}</li>
      </ul>
    `,s.appendChild(r),s.addEventListener("secondary-button-click",J),s.addEventListener("primary-button-click",Y),o.innerHTML="<slot></slot>",o.appendChild(s)}},y=(o,e)=>{e&&e.querySelector("vaadin-pretrial")?.setAttribute("start-failure",o?"expired":"")},S=(o,e)=>{e&&e.querySelector("vaadin-pretrial")?.setAttribute("license-download",o)},x=1e3,k=(o,e)=>{const t=Array.from(o.querySelectorAll(e.join(", "))),s=Array.from(o.querySelectorAll("*")).filter(r=>r.shadowRoot).flatMap(r=>k(r.shadowRoot,e));return[...t,...s]};let I=!1;const v=(o,e)=>{I||(window.addEventListener("message",r=>{r.data==="validate-license"&&window.location.reload()},!1),I=!0);const t=o._overlayElement;if(t){if(t.shadowRoot){const r=t.shadowRoot.querySelector("slot:not([name])");if(r&&r.assignedElements().length>0){v(r.assignedElements()[0],e);return}}v(t,e);return}const s=e.messageHtml?e.messageHtml:`${e.message} <p>Component: ${e.product.name} ${e.product.version}</p>`.replace(/https:([^ ]*)/g,"<a href='https:$1'>https:$1</a>");o.isConnected&&(o.outerHTML=`<no-license style="display:flex;align-items:center;text-align:center;justify-content:center;"><div>${s}</div></no-license>`)},g={},_={},m={},M={},h=o=>`${o.name}_${o.version}`,L=o=>{const{cvdlName:e,version:t}=o.constructor,s={name:e,version:t},r=o.tagName.toLowerCase();g[e]=g[e]??[],g[e].push(r);const n=m[h(s)];n&&setTimeout(()=>v(o,n),x),m[h(s)]||M[h(s)]||_[h(s)]||(_[h(s)]=!0,window.Vaadin.devTools.checkLicense(s))},G=o=>{M[h(o)]=!0,console.debug("License check ok for",o)},Q=o=>{const e=o.product.name;m[h(o.product)]=o,console.error("License check failed for",e);const t=g[e];t?.length>0&&k(document,t).forEach(s=>{setTimeout(()=>v(s,m[h(o.product)]),x)})},ee=o=>{const e=o.message,t=o.product.name;o.messageHtml=`No license found. <a target=_blank onclick="javascript:window.open(this.href);return false;" href="${e}">Go here to start a trial or retrieve your license.</a>`,m[h(o.product)]=o,console.error("No license found when checking",t);const s=g[t];s?.length>0&&k(document,s).forEach(r=>{setTimeout(()=>v(r,m[h(o.product)]),x)})},te=(o,e)=>o.command==="license-check-ok"?(G(o.data),!0):o.command==="license-check-failed"?(Q(o.data),!0):o.command==="license-check-nokey"?(X(e,o.data),ee(o.data),!0):o.command==="license-pretrial-started"?(console.debug("Pre-trial period started",o.data),window.location.reload(),!0):o.command==="license-pretrial-expired"?(console.debug("Pre-trial period expired",o.data),y(!0,e),!0):o.command==="license-pretrial-failed"?(console.debug("Pre-trial period start failed",o.data),y(!1,e),!0):o.command==="license-download-completed"?(console.debug("License downloaded"),window.location.reload(),!0):o.command==="license-download-started"?(S("started",e),!0):o.command==="license-download-failed"?(S("failed",e),!0):!1,oe=()=>{window.Vaadin.devTools.startPreTrial()},U=()=>{const o=Object.values(m);o.length>0&&window.Vaadin.devTools.downloadLicense(o[0].product)},se=()=>{window.Vaadin.devTools.createdCvdlElements.forEach(o=>{L(o)}),window.Vaadin.devTools.createdCvdlElements={push:o=>{L(o)}}};var i;(function(o){o.ACTIVE="active",o.INACTIVE="inactive",o.UNAVAILABLE="unavailable",o.ERROR="error"})(i||(i={}));const T=class T{constructor(){this.status=i.UNAVAILABLE}onHandshake(){}onConnectionError(e){}onStatusChange(e){}setActive(e){!e&&this.status===i.ACTIVE?this.setStatus(i.INACTIVE):e&&this.status===i.INACTIVE&&this.setStatus(i.ACTIVE)}setStatus(e){this.status!==e&&(this.status=e,this.onStatusChange(e))}};T.HEARTBEAT_INTERVAL=18e4;let w=T;class ne extends w{constructor(e){super(),this.webSocket=new WebSocket(e),this.webSocket.onmessage=t=>this.handleMessage(t),this.webSocket.onerror=t=>this.handleError(t),this.webSocket.onclose=t=>{this.status!==i.ERROR&&this.setStatus(i.UNAVAILABLE),this.webSocket=void 0},setInterval(()=>{this.webSocket&&self.status!==i.ERROR&&this.status!==i.UNAVAILABLE&&this.webSocket.send("")},w.HEARTBEAT_INTERVAL)}onReload(e){}handleMessage(e){let t;try{t=JSON.parse(e.data)}catch(s){this.handleError(`[${s.name}: ${s.message}`);return}if(t.command==="hello")this.setStatus(i.ACTIVE),this.onHandshake();else if(t.command==="reload"){if(this.status===i.ACTIVE){const s=t.strategy||"reload";this.onReload(s)}}else this.handleError(`Unknown message from the livereload server: ${e}`)}handleError(e){console.error(e),this.setStatus(i.ERROR),e instanceof Event&&this.webSocket?this.onConnectionError(`Error in WebSocket connection to ${this.webSocket.url}`):this.onConnectionError(e)}}const R=16384,A=class A extends w{constructor(e){if(super(),this.canSend=!1,!e)return;const t={transport:"websocket",fallbackTransport:"websocket",url:e,contentType:"application/json; charset=UTF-8",reconnectInterval:5e3,timeout:-1,maxReconnectOnClose:1e7,trackMessageLength:!0,enableProtocol:!0,handleOnlineOffline:!1,executeCallbackBeforeReconnect:!0,messageDelimiter:"|",onMessage:s=>{const r={data:s.responseBody};this.handleMessage(r)},onError:s=>{this.canSend=!1,this.handleError(s)},onOpen:()=>{this.canSend=!0},onClose:()=>{this.canSend=!1},onClientTimeout:()=>{this.canSend=!1},onReconnect:()=>{this.canSend=!1},onReopen:()=>{this.canSend=!0}};re().then(s=>{this.socket=s.subscribe(t)})}onReload(e){}onUpdate(e,t){}onMessage(e){}handleMessage(e){let t;try{t=JSON.parse(e.data)}catch(s){this.handleError(`[${s.name}: ${s.message}`);return}if(t.command==="hello")this.setStatus(i.ACTIVE),this.onHandshake();else if(t.command==="reload"){if(this.status===i.ACTIVE){const s=t.strategy||"reload";this.onReload(s)}}else t.command==="update"?this.status===i.ACTIVE&&this.onUpdate(t.path,t.content):this.onMessage(t)}handleError(e){console.error(e),this.setStatus(i.ERROR),this.onConnectionError(e)}send(e,t){if(!this.socket||!this.canSend){C(()=>this.socket&&this.canSend,a=>this.send(e,t));return}const s=JSON.stringify({command:e,data:t});let n=s.length+"|"+s;for(;n.length;)this.socket.push(n.substring(0,R)),n=n.substring(R)}};A.HEARTBEAT_INTERVAL=18e4;let E=A;function C(o,e){const t=o();t?e(t):setTimeout(()=>C(o,e),50)}function re(){return new Promise((o,e)=>{C(()=>window?.vaadinPush?.atmosphere,o)})}const ie=1,ae="vaadin-refresh-ui";function le(o){if(o.id)return"#"+CSS.escape(o.id);const e=[];let t=o;for(;t&&t!==document.documentElement&&t!==document.body;){if(t.id){e.unshift("#"+CSS.escape(t.id));break}const s=t.parentElement;if(!s)break;let r=1,n=t.previousElementSibling;for(;n;)n.tagName===t.tagName&&r++,n=n.previousElementSibling;e.unshift(t.tagName.toLowerCase()+":nth-of-type("+r+")"),t=s}return e.length>0?e.join(" > "):""}function F(){const o=window.Vaadin;return Object.keys(o?.Flow?.clients||{}).filter(e=>e!=="TypeScript").map(e=>o.Flow.clients[e])}function $(){const o={};return(window.scrollX!==0||window.scrollY!==0)&&(o.__window__={scrollTop:window.scrollY,scrollLeft:window.scrollX}),document.querySelectorAll("*").forEach(e=>{if(e.scrollTop>0||e.scrollLeft>0){const t=le(e);t&&(o[t]={scrollTop:e.scrollTop,scrollLeft:e.scrollLeft})}}),o}function B(o){const e=$();F().forEach(t=>{t.sendEventMessage&&t.sendEventMessage(ie,"ui-refresh",{fullRefresh:o})}),H(e)}let O=!1;function de(){O||(O=!0,window.addEventListener(ae,o=>{B(o.detail?.fullRefresh===!0)}))}function H(o){if(Object.keys(o).length===0)return;const e=200;let t=0;const s=()=>{requestAnimationFrame(()=>{for(const[n,a]of Object.entries(o))if(n==="__window__")window.scrollTo(a.scrollLeft,a.scrollTop);else{const l=document.querySelector(n);l&&(l.scrollTop=a.scrollTop,l.scrollLeft=a.scrollLeft)}})},r=()=>{const n=F();n.length>0&&n.every(l=>!l.isActive())||++t>=e?s():setTimeout(r,50)};setTimeout(r,50)}var c,N;(function(o){o.LOG="log",o.INFORMATION="information",o.WARNING="warning",o.ERROR="error"})(N||(N={}));const P=import.meta.hot?import.meta.hot.hmrClient:void 0;var d;let p=(d=class extends D{constructor(){super(...arguments),this.unhandledMessages=[],this.conf={enable:!1,url:"",contextRelativePath:"",liveReloadPort:-1},this.bodyShadowRoot=null,this.frontendStatus=i.UNAVAILABLE,this.javaStatus=i.UNAVAILABLE,this.componentPickActive=!1,this.nextMessageId=1,this.transitionDuration=0}static get styles(){return[j`
        :host {
          --dev-tools-font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen-Sans, Ubuntu, Cantarell,
            'Helvetica Neue', sans-serif;
          --dev-tools-font-family-monospace: SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New',
            monospace;

          --dev-tools-font-size: 0.8125rem;
          --dev-tools-font-size-small: 0.75rem;

          --dev-tools-text-color: rgba(255, 255, 255, 0.8);
          --dev-tools-text-color-secondary: rgba(255, 255, 255, 0.65);
          --dev-tools-text-color-emphasis: rgba(255, 255, 255, 0.95);
          --dev-tools-text-color-active: rgba(255, 255, 255, 1);

          --dev-tools-background-color-inactive: rgba(45, 45, 45, 0.25);
          --dev-tools-background-color-active: rgba(45, 45, 45, 0.98);
          --dev-tools-background-color-active-blurred: rgba(45, 45, 45, 0.85);

          --dev-tools-border-radius: 0.5rem;
          --dev-tools-box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.05), 0 4px 12px -2px rgba(0, 0, 0, 0.4);

          --dev-tools-blue-hsl: 206, 100%, 70%;
          --dev-tools-blue-color: hsl(var(--dev-tools-blue-hsl));
          --dev-tools-green-hsl: 145, 80%, 42%;
          --dev-tools-green-color: hsl(var(--dev-tools-green-hsl));
          --dev-tools-grey-hsl: 0, 0%, 50%;
          --dev-tools-grey-color: hsl(var(--dev-tools-grey-hsl));
          --dev-tools-yellow-hsl: 38, 98%, 64%;
          --dev-tools-yellow-color: hsl(var(--dev-tools-yellow-hsl));
          --dev-tools-red-hsl: 355, 100%, 68%;
          --dev-tools-red-color: hsl(var(--dev-tools-red-hsl));

          /* Needs to be in ms, used in JavaScript as well */
          --dev-tools-transition-duration: 180ms;

          all: initial;

          direction: ltr;
          cursor: default;
          font: normal 400 var(--dev-tools-font-size) / 1.125rem var(--dev-tools-font-family);
          color: var(--dev-tools-text-color);
          -webkit-user-select: none;
          -moz-user-select: none;
          user-select: none;
          color-scheme: dark;

          position: fixed;
          z-index: 20000;
          pointer-events: none;
          bottom: 0;
          right: 0;
          width: 100%;
          height: 100%;
          display: flex;
          flex-direction: column-reverse;
          align-items: flex-end;
        }

        .dev-tools {
          pointer-events: auto;
          display: flex;
          align-items: center;
          position: fixed;
          z-index: inherit;
          right: 0.5rem;
          bottom: 0.5rem;
          min-width: 1.75rem;
          height: 1.75rem;
          max-width: 1.75rem;
          border-radius: 0.5rem;
          padding: 0.375rem;
          box-sizing: border-box;
          background-color: var(--dev-tools-background-color-inactive);
          box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.05);
          color: var(--dev-tools-text-color);
          transition: var(--dev-tools-transition-duration);
          white-space: nowrap;
          line-height: 1rem;
        }

        .dev-tools:hover,
        .dev-tools.active {
          background-color: var(--dev-tools-background-color-active);
          box-shadow: var(--dev-tools-box-shadow);
        }

        .dev-tools.active {
          max-width: calc(100% - 1rem);
        }

        .dev-tools .status-description {
          overflow: hidden;
          text-overflow: ellipsis;
          padding: 0 0.25rem;
        }

        .dev-tools.error {
          background-color: hsla(var(--dev-tools-red-hsl), 0.15);
          animation: bounce 0.5s;
          animation-iteration-count: 2;
        }

        .window.hidden {
          opacity: 0;
          transform: scale(0);
          position: absolute;
        }

        .window.visible {
          transform: none;
          opacity: 1;
          pointer-events: auto;
        }

        .window.visible ~ .dev-tools {
          opacity: 0;
          pointer-events: none;
        }

        .window.visible ~ .dev-tools .dev-tools-icon,
        .window.visible ~ .dev-tools .status-blip {
          transition: none;
          opacity: 0;
        }

        .window {
          border-radius: var(--dev-tools-border-radius);
          overflow: auto;
          margin: 0.5rem;
          min-width: 30rem;
          max-width: calc(100% - 1rem);
          max-height: calc(100vh - 1rem);
          flex-shrink: 1;
          background-color: var(--dev-tools-background-color-active);
          color: var(--dev-tools-text-color);
          transition: var(--dev-tools-transition-duration);
          transform-origin: bottom right;
          display: flex;
          flex-direction: column;
          box-shadow: var(--dev-tools-box-shadow);
          outline: none;
        }

        .window-toolbar {
          display: flex;
          flex: none;
          align-items: center;
          padding: 0.375rem;
          white-space: nowrap;
          order: 1;
          background-color: rgba(0, 0, 0, 0.2);
          gap: 0.5rem;
        }

        .ahreflike {
          font-weight: 500;
          color: var(--dev-tools-text-color-secondary);
          text-decoration: underline;
          cursor: pointer;
        }

        .ahreflike:hover {
          color: var(--dev-tools-text-color-emphasis);
        }

        .button {
          all: initial;
          font-family: inherit;
          font-size: var(--dev-tools-font-size-small);
          line-height: 1;
          white-space: nowrap;
          background-color: rgba(0, 0, 0, 0.2);
          color: inherit;
          font-weight: 600;
          padding: 0.25rem 0.375rem;
          border-radius: 0.25rem;
        }

        .button:focus,
        .button:hover {
          color: var(--dev-tools-text-color-emphasis);
        }

        .message.information {
          --dev-tools-notification-color: var(--dev-tools-blue-color);
        }

        .message.warning {
          --dev-tools-notification-color: var(--dev-tools-yellow-color);
        }

        .message.error {
          --dev-tools-notification-color: var(--dev-tools-red-color);
        }

        .message {
          display: flex;
          padding: 0.1875rem 0.75rem 0.1875rem 2rem;
          background-clip: padding-box;
        }

        .message.log {
          padding-left: 0.75rem;
        }

        .message-content {
          margin-right: 0.5rem;
          -webkit-user-select: text;
          -moz-user-select: text;
          user-select: text;
        }

        .message-heading {
          position: relative;
          display: flex;
          align-items: center;
          margin: 0.125rem 0;
        }

        .message.log {
          color: var(--dev-tools-text-color-secondary);
        }

        .message:not(.log) .message-heading {
          font-weight: 500;
        }

        .message.has-details .message-heading {
          color: var(--dev-tools-text-color-emphasis);
          font-weight: 600;
        }

        .message-heading::before {
          position: absolute;
          margin-left: -1.5rem;
          display: inline-block;
          text-align: center;
          font-size: 0.875em;
          font-weight: 600;
          line-height: calc(1.25em - 2px);
          width: 14px;
          height: 14px;
          box-sizing: border-box;
          border: 1px solid transparent;
          border-radius: 50%;
        }

        .message.information .message-heading::before {
          content: 'i';
          border-color: currentColor;
          color: var(--dev-tools-notification-color);
        }

        .message.warning .message-heading::before,
        .message.error .message-heading::before {
          content: '!';
          color: var(--dev-tools-background-color-active);
          background-color: var(--dev-tools-notification-color);
        }

        .features-tray {
          padding: 0.75rem;
          flex: auto;
          overflow: auto;
          animation: fade-in var(--dev-tools-transition-duration) ease-in;
          user-select: text;
        }

        .features-tray p {
          margin-top: 0;
          color: var(--dev-tools-text-color-secondary);
        }

        .features-tray .feature {
          display: flex;
          align-items: center;
          gap: 1rem;
          padding-bottom: 0.5em;
        }

        .message .message-details {
          font-weight: 400;
          color: var(--dev-tools-text-color-secondary);
          margin: 0.25rem 0;
        }

        .message .message-details[hidden] {
          display: none;
        }

        .message .message-details p {
          display: inline;
          margin: 0;
          margin-right: 0.375em;
          word-break: break-word;
        }

        .message .persist {
          color: var(--dev-tools-text-color-secondary);
          white-space: nowrap;
          margin: 0.375rem 0;
          display: flex;
          align-items: center;
          position: relative;
          -webkit-user-select: none;
          -moz-user-select: none;
          user-select: none;
        }

        .message .persist::before {
          content: '';
          width: 1em;
          height: 1em;
          border-radius: 0.2em;
          margin-right: 0.375em;
          background-color: rgba(255, 255, 255, 0.3);
        }

        .message .persist:hover::before {
          background-color: rgba(255, 255, 255, 0.4);
        }

        .message .persist.on::before {
          background-color: rgba(255, 255, 255, 0.9);
        }

        .message .persist.on::after {
          content: '';
          order: -1;
          position: absolute;
          width: 0.75em;
          height: 0.25em;
          border: 2px solid var(--dev-tools-background-color-active);
          border-width: 0 0 2px 2px;
          transform: translate(0.05em, -0.05em) rotate(-45deg) scale(0.8, 0.9);
        }

        .message .dismiss-message {
          font-weight: 600;
          align-self: stretch;
          display: flex;
          align-items: center;
          padding: 0 0.25rem;
          margin-left: 0.5rem;
          color: var(--dev-tools-text-color-secondary);
        }

        .message .dismiss-message:hover {
          color: var(--dev-tools-text-color);
        }

        .notification-tray {
          display: flex;
          flex-direction: column-reverse;
          align-items: flex-end;
          margin: 0.5rem;
          flex: none;
        }

        .window.hidden + .notification-tray {
          margin-bottom: 3rem;
        }

        .notification-tray .message {
          pointer-events: auto;
          background-color: var(--dev-tools-background-color-active);
          color: var(--dev-tools-text-color);
          max-width: 30rem;
          box-sizing: border-box;
          border-radius: var(--dev-tools-border-radius);
          margin-top: 0.5rem;
          transition: var(--dev-tools-transition-duration);
          transform-origin: bottom right;
          animation: slideIn var(--dev-tools-transition-duration);
          box-shadow: var(--dev-tools-box-shadow);
          padding-top: 0.25rem;
          padding-bottom: 0.25rem;
        }

        .notification-tray .message.animate-out {
          animation: slideOut forwards var(--dev-tools-transition-duration);
        }

        .notification-tray .message .message-details {
          max-height: 10em;
          overflow: hidden;
        }

        .message-tray {
          flex: auto;
          overflow: auto;
          max-height: 20rem;
          user-select: text;
        }

        .message-tray .message {
          animation: fade-in var(--dev-tools-transition-duration) ease-in;
          padding-left: 2.25rem;
        }

        .message-tray .message.warning {
          background-color: hsla(var(--dev-tools-yellow-hsl), 0.09);
        }

        .message-tray .message.error {
          background-color: hsla(var(--dev-tools-red-hsl), 0.09);
        }

        .message-tray .message.error .message-heading {
          color: hsl(var(--dev-tools-red-hsl));
        }

        .message-tray .message.warning .message-heading {
          color: hsl(var(--dev-tools-yellow-hsl));
        }

        .message-tray .message + .message {
          border-top: 1px solid rgba(255, 255, 255, 0.07);
        }

        .message-tray .dismiss-message,
        .message-tray .persist {
          display: none;
        }

        @keyframes slideIn {
          from {
            transform: translateX(100%);
            opacity: 0;
          }
          to {
            transform: translateX(0%);
            opacity: 1;
          }
        }

        @keyframes slideOut {
          from {
            transform: translateX(0%);
            opacity: 1;
          }
          to {
            transform: translateX(100%);
            opacity: 0;
          }
        }

        @keyframes fade-in {
          0% {
            opacity: 0;
          }
        }

        @keyframes bounce {
          0% {
            transform: scale(0.8);
          }
          50% {
            transform: scale(1.5);
            background-color: hsla(var(--dev-tools-red-hsl), 1);
          }
          100% {
            transform: scale(1);
          }
        }

        @supports (backdrop-filter: blur(1px)) {
          .dev-tools,
          .window,
          .notification-tray .message {
            backdrop-filter: blur(8px);
          }

          .dev-tools:hover,
          .dev-tools.active,
          .window,
          .notification-tray .message {
            background-color: var(--dev-tools-background-color-active-blurred);
          }
        }
      `]}static get isActive(){const e=window.sessionStorage.getItem(c.ACTIVE_KEY_IN_SESSION_STORAGE);return e===null||e!=="false"}elementTelemetry(){let e={};try{const t=localStorage.getItem("vaadin.statistics.basket");if(!t)return;e=JSON.parse(t)}catch{return}this.frontendConnection&&this.frontendConnection.send("reportTelemetry",{browserData:e})}openWebSocketConnection(){if(this.frontendStatus=i.UNAVAILABLE,this.javaStatus=i.UNAVAILABLE,!this.conf.token){console.error("Dev tools functionality denied for this host. See Vaadin documentation on how to configure devmode.hostsAllowed property: https://vaadin.com/docs/latest/configuration/properties#properties");return}const e=n=>console.error(n),t=(n="reload")=>{if(n==="refresh"||n==="full-refresh")B(n==="full-refresh");else{const a=$();window.sessionStorage.setItem("vaadin-hotswap-scroll",JSON.stringify(a));const l=window.sessionStorage.getItem(c.TRIGGERED_COUNT_KEY_IN_SESSION_STORAGE),f=l?parseInt(l,10)+1:1;window.sessionStorage.setItem(c.TRIGGERED_COUNT_KEY_IN_SESSION_STORAGE,f.toString()),window.sessionStorage.setItem(c.TRIGGERED_KEY_IN_SESSION_STORAGE,"true"),window.location.reload()}},s=(n,a)=>{const l="context://",f=n.substring(l.length);if(n.startsWith(l)&&(n=this.conf.contextRelativePath+f),a){let u=document.head.querySelector(`style[data-file-path='${n}']`);u||(u=document.createElement("style"),u.setAttribute("data-file-path",n),document.head.appendChild(u),this.removeOldLinks(f)),u.textContent=a,document.dispatchEvent(new CustomEvent("vaadin-theme-updated"))}else if(a===""||a===null){const u=document.head.querySelector(`style[data-file-path='${n}']`);u?u.remove():this.removeOldLinks(f),document.dispatchEvent(new CustomEvent("vaadin-theme-updated"))}},r=new E(this.getDedicatedWebSocketUrl());r.onHandshake=()=>{c.isActive||r.setActive(!1),this.conf.usageStatisticsEnabled===!1?(localStorage.setItem("vaadin.statistics.optout","true"),localStorage.removeItem("vaadin.statistics.basket"),localStorage.removeItem("vaadin.statistics.firstuse")):localStorage.removeItem("vaadin.statistics.optout"),this.elementTelemetry()},r.onConnectionError=e,r.onReload=t,r.onUpdate=s,r.onStatusChange=n=>{this.frontendStatus=n},r.onMessage=n=>this.handleFrontendMessage(n),this.frontendConnection=r,this.conf.backend===c.SPRING_BOOT_DEVTOOLS&&this.conf.liveReloadPort&&(this.javaConnection=new ne(this.getSpringBootWebSocketUrl(window.location)),this.javaConnection.onHandshake=()=>{c.isActive||this.javaConnection.setActive(!1)},this.javaConnection.onReload=t,this.javaConnection.onConnectionError=e,this.javaConnection.onStatusChange=n=>{this.javaStatus=n})}removeOldLinks(e){Array.from(document.head.querySelectorAll('link[rel="stylesheet"]')).forEach(s=>{let r=s.getAttribute("data-file-path")||s.getAttribute("href");if(r){const n=r.split(/[?#]/)[0];(n===e||n.endsWith("/"+e))&&s.remove()}})}tabHandleMessage(e,t){const s=e;return s.handleMessage&&s.handleMessage.call(e,t)}handleFrontendMessage(e){e.command==="featureFlags"||te(e,this.bodyShadowRoot)||this.handleHmrMessage(e)||this.unhandledMessages.push(e)}handleHmrMessage(e){return e.command!=="hmr"?!1:(P&&P.notifyListeners(e.data.event,e.data.eventData),!0)}getDedicatedWebSocketUrl(){function e(s){const r=document.createElement("div");return r.innerHTML=`<a href="${s}"/>`,r.firstChild.href}if(this.conf.url===void 0)return;const t=e(this.conf.url);if(!t.startsWith("http://")&&!t.startsWith("https://")){console.error("The protocol of the url should be http or https for live reload to work.");return}return`${t}?v-r=push&debug_window&token=${this.conf.token}`}getSpringBootWebSocketUrl(e){const{hostname:t}=e,s=e.protocol==="https:"?"wss":"ws";if(t.endsWith("gitpod.io")){const r=t.replace(/.*?-/,"");return`${s}://${this.conf.liveReloadPort}-${r}`}else return`${s}://${t}:${this.conf.liveReloadPort}`}connectedCallback(){if(super.connectedCallback(),this.bodyShadowRoot=document.body.attachShadow({mode:"closed"}),this.bodyShadowRoot.innerHTML="<slot></slot>",this.conf=window.Vaadin.devToolsConf||this.conf,window.sessionStorage.getItem(c.TRIGGERED_KEY_IN_SESSION_STORAGE)){const n=new Date;`${`0${n.getHours()}`.slice(-2)}${`0${n.getMinutes()}`.slice(-2)}${`0${n.getSeconds()}`.slice(-2)}`,window.sessionStorage.removeItem(c.TRIGGERED_KEY_IN_SESSION_STORAGE)}const t=window.sessionStorage.getItem("vaadin-hotswap-scroll");t!==null&&(window.sessionStorage.removeItem("vaadin-hotswap-scroll"),H(JSON.parse(t))),de(),this.transitionDuration=parseInt(window.getComputedStyle(this).getPropertyValue("--dev-tools-transition-duration"),10);const s=window;s.Vaadin=s.Vaadin||{},s.Vaadin.devTools=Object.assign(this,s.Vaadin.devTools);const r=window.Vaadin;r.devToolsPlugins&&(Array.from(r.devToolsPlugins).forEach(n=>this.initPlugin(n)),r.devToolsPlugins={push:n=>this.initPlugin(n)}),this.openWebSocketConnection(),se()}async initPlugin(e){const t=this;e.init({send:function(s,r){t.frontendConnection.send(s,r)}})}format(e){return e.toString()}checkLicense(e){this.frontendConnection?this.frontendConnection.send("checkLicense",e):G(e)}startPreTrial(){this.frontendConnection?this.frontendConnection.send("startPreTrialLicense",{}):(console.error("Cannot start pre-trial: no connection"),y(!1,this.bodyShadowRoot))}downloadLicense(e){this.frontendConnection?this.frontendConnection.send("downloadLicense",e):S("failed",this.bodyShadowRoot)}setActive(e){this.frontendConnection?.setActive(e),this.javaConnection?.setActive(e),window.sessionStorage.setItem(c.ACTIVE_KEY_IN_SESSION_STORAGE,e?"true":"false")}render(){return z` <div style="display: none" class="dev-tools"></div>`}setJavaLiveReloadActive(e){this.javaConnection?this.javaConnection.setActive(e):this.frontendConnection?.setActive(e)}},c=d,d.DISMISSED_NOTIFICATIONS_IN_LOCAL_STORAGE="vaadin.live-reload.dismissedNotifications",d.ACTIVE_KEY_IN_SESSION_STORAGE="vaadin.live-reload.active",d.TRIGGERED_KEY_IN_SESSION_STORAGE="vaadin.live-reload.triggered",d.TRIGGERED_COUNT_KEY_IN_SESSION_STORAGE="vaadin.live-reload.triggeredCount",d.AUTO_DEMOTE_NOTIFICATION_DELAY=5e3,d.HOTSWAP_AGENT="HOTSWAP_AGENT",d.JREBEL="JREBEL",d.SPRING_BOOT_DEVTOOLS="SPRING_BOOT_DEVTOOLS",d.BACKEND_DISPLAY_NAME={HOTSWAP_AGENT:"HotswapAgent",JREBEL:"JRebel",SPRING_BOOT_DEVTOOLS:"Spring Boot Devtools"},d);b([V({type:String,attribute:!1})],p.prototype,"frontendStatus",void 0);b([V({type:String,attribute:!1})],p.prototype,"javaStatus",void 0);b([W(".window")],p.prototype,"root",void 0);b([q()],p.prototype,"componentPickActive",void 0);p=c=b([Z("vaadin-dev-tools")],p);
//# sourceMappingURL=vaadin-dev-tools.js.map
