import { e as v, $ as o, N as g, x as n, p as f, ai as y, n as x } from "./copilot-Bm5k-h74.js";
import { r as m } from "./state-BH_9RcTJ.js";
import { B as w } from "./base-panel-Cxtbncvf.js";
import { i as c } from "./icons-mWh87_oB.js";
function b(e) {
  return v("copilot-switch-user", { username: e }, (t) => t.data.error ? { success: !1, errorMessage: t.data.error.message } : { success: !0 });
}
const u = "copilot-impersonator{display:flex;flex-direction:column;height:100%;overflow:auto}.impersonator-container{display:flex;flex-direction:column;gap:var(--lumo-space-l);padding:var(--lumo-space-m)}.impersonator-section{display:flex;flex-direction:column;gap:var(--lumo-space-s)}.section-title{margin:0;font-size:var(--lumo-font-size-m);font-weight:600;color:var(--lumo-body-text-color)}.section-description{margin:0;font-size:var(--lumo-font-size-s);color:var(--lumo-secondary-text-color)}.username-field{width:100%}.recent-users-list{display:flex;flex-direction:column;gap:var(--lumo-space-xs)}.recent-user-item{display:flex;align-items:center;gap:var(--lumo-space-xs);border-radius:var(--lumo-border-radius-m);background-color:var(--lumo-contrast-5pct);padding:var(--lumo-space-xs)}.recent-user-button{flex:1;justify-content:flex-start;margin:0}.empty-state{display:flex;flex-direction:column;align-items:center;justify-content:center;text-align:center;padding:var(--lumo-space-xl);gap:var(--lumo-space-m);min-height:300px}.empty-icon{width:64px;height:64px;color:var(--lumo-contrast-30pct)}.empty-state h3{margin:0;font-size:var(--lumo-font-size-l);font-weight:600;color:var(--lumo-body-text-color)}.empty-state p{margin:0;font-size:var(--lumo-font-size-s);color:var(--lumo-secondary-text-color);max-width:400px}";
var U = Object.defineProperty, $ = Object.getOwnPropertyDescriptor, l = (e, t, i, r) => {
  for (var s = r > 1 ? void 0 : r ? $(t, i) : t, d = e.length - 1, p; d >= 0; d--)
    (p = e[d]) && (s = (r ? p(t, i, s) : p(s)) || s);
  return r && s && U(t, i, s), s;
};
let a = class extends w {
  constructor() {
    super(...arguments), this.username = "", this.errorMessage = "", this.isLoading = !1, this.handleKeyDown = async (e) => {
      e.key === "Enter" && this.username && !this.isLoading && await this.handleSwitchUser();
    }, this.handleSwitchUser = async () => {
      if (!(!this.username || this.isLoading)) {
        this.isLoading = !0, this.errorMessage = "";
        try {
          const e = await b(this.username);
          e.success ? (o.addRecentSwitchedUsername(this.username), window.location.reload()) : (this.errorMessage = e.errorMessage, this.isLoading = !1);
        } catch {
          this.errorMessage = "An unexpected error occurred", this.isLoading = !1;
        }
      }
    }, this.switchToRecentUser = async (e) => {
      this.username = e, await this.handleSwitchUser();
    }, this.removeRecentUser = (e, t) => {
      t.stopPropagation(), o.removeRecentSwitchedUsername(e), this.requestUpdate();
    };
  }
  connectedCallback() {
    super.connectedCallback(), this.reaction(
      () => o.getRecentSwitchedUsernames(),
      () => {
        this.requestUpdate();
      }
    );
  }
  render() {
    if (!g.springSecurityEnabled)
      return n`
        <style>
          ${u}
        </style>
        <div class="impersonator-container">
          <div class="empty-state">
            <vaadin-icon class="empty-icon" .svg="${c.accountCircle}"></vaadin-icon>
            <h3>Spring Security Not Enabled</h3>
            <p>User impersonation requires Spring Security to be configured in your application.</p>
          </div>
        </div>
      `;
    const e = o.getRecentSwitchedUsernames();
    return n`
      <style>
        ${u}
      </style>
      <div class="impersonator-container">
        <div class="impersonator-section">
          <h3 class="section-title">Switch to User</h3>
          <p class="section-description">Enter a username to impersonate in your application.</p>

          <vaadin-text-field
            class="username-field"
            label="Username"
            placeholder="Enter username"
            .value="${this.username}"
            .errorMessage="${this.errorMessage}"
            .invalid="${this.errorMessage !== ""}"
            ?disabled="${this.isLoading}"
            @value-changed="${(t) => {
      this.username = t.detail.value, this.errorMessage = "";
    }}"
            @keydown="${this.handleKeyDown}">
            <vaadin-icon slot="prefix" .svg="${c.accountCircle}"></vaadin-icon>
          </vaadin-text-field>

          <vaadin-button
            theme="primary"
            ?disabled="${!this.username || this.isLoading}"
            @click="${this.handleSwitchUser}">
            ${this.isLoading ? "Switching..." : "Switch User"}
          </vaadin-button>
        </div>

        ${e.length > 0 ? n`
              <div class="impersonator-section">
                <h3 class="section-title">Recently Used Usernames</h3>
                <div class="recent-users-list">
                  ${e.map(
      (t) => n`
                      <div class="recent-user-item">
                        <vaadin-button
                          class="recent-user-button"
                          theme="tertiary"
                          @click="${() => this.switchToRecentUser(t)}">
                          <vaadin-icon slot="prefix" .svg="${c.accountCircle}"></vaadin-icon>
                          ${t}
                        </vaadin-button>
                        <vaadin-button
                          theme="icon tertiary"
                          aria-label="Remove ${t}"
                          @click="${(i) => this.removeRecentUser(t, i)}">
                          <vaadin-icon .svg="${c.delete}"></vaadin-icon>
                          <vaadin-tooltip slot="tooltip" text="Remove from recent"></vaadin-tooltip>
                        </vaadin-button>
                      </div>
                    `
    )}
                </div>
              </div>
            ` : ""}
      </div>
    `;
  }
};
l([
  m()
], a.prototype, "username", 2);
l([
  m()
], a.prototype, "errorMessage", 2);
l([
  m()
], a.prototype, "isLoading", 2);
a = l([
  x("copilot-impersonator")
], a);
const h = {
  header: "Impersonate User",
  tag: y.IMPERSONATOR,
  individual: !0,
  toolbarOptions: {
    allowedModesWithOrder: {
      common: 0
    },
    iconKey: "accountCircle"
  }
}, S = {
  init(e) {
    e.addPanel(h);
  }
};
window.Vaadin.copilot.plugins.push(S);
f.addPanel(h);
export {
  a as CopilotImpersonatorPanel
};
