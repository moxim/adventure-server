import { $ as l, q as i, E as u, x as o, y as v, z as y, af as w, D as T, a4 as I, M as q, ag as A, ah as C, n as f } from "./copilot-Bm5k-h74.js";
import { r as h } from "./state-BH_9RcTJ.js";
import { B as O } from "./base-panel-Cxtbncvf.js";
import { i as x } from "./icons-mWh87_oB.js";
const n = (a) => i.userInfo?.copilotExperimentalFeatureFlag === !0 && l.isExperimentalFeatureEnabled(a), F = {
  id: "theme-from-image",
  name: "Theme from Image",
  description: "Generate a custom theme based on an image you provide.",
  enabled: () => n(F),
  available: () => i.appTheme === "lumo",
  requiresReload: !1
}, R = {
  id: "ai-docs-assistant",
  name: "AI Docs Assistant",
  description: "AI-powered Vaadin documentation assistant.",
  enabled: () => n(R),
  available: () => !0,
  requiresReload: !1
}, $ = {
  id: "testbench-test-recorder",
  name: "TestBench Test Recorder",
  description: "Record user interactions to generate end-to-end Vaadin TestBench tests automatically.",
  enabled: () => n($),
  available: () => !0,
  requiresReload: !0
}, E = {
  id: "i18n",
  name: "Internationalization",
  description: "Edit and manage translations for your application.",
  enabled: () => n(E),
  available: () => !0,
  requiresReload: !0
}, M = [
  F,
  R,
  $,
  E
];
var S = Object.defineProperty, D = Object.getOwnPropertyDescriptor, d = (a, e, t, r) => {
  for (var s = r > 1 ? void 0 : r ? D(e, t) : e, c = a.length - 1, p; c >= 0; c--)
    (p = a[c]) && (s = (r ? p(e, t, s) : p(s)) || s);
  return r && s && S(e, t, s), s;
};
const g = window.Vaadin.devTools;
let m = class extends O {
  constructor() {
    super(...arguments), this.toggledFeaturesThatAreRequiresServerRestart = [];
  }
  connectedCallback() {
    super.connectedCallback(), this.classList.add("contents");
  }
  render() {
    const a = i.userInfo?.copilotExperimentalFeatureFlag;
    return o`
      <div class="flex flex-col gap-6 px-4 py-0.5">
        <div class="border-dashed flex flex-col divide-y">
          ${i.featureFlags.slice().sort((e, t) => e.title.localeCompare(t.title)).map(
      (e) => o`
                <div class="flex gap-2 justify-between py-3.5">
                  <div class="flex flex-col">
                    <label id="${e.id}-label">${e.title}</label>
                    <a
                      class="flex gap-0.5 text-xs"
                      href="${e.moreInfoLink}"
                      id="${e.id}-desc"
                      target="_blank"
                      rel="noopener noreferrer"
                      >More info<vaadin-icon class="icon-sm" .svg="${x.arrowOutward}"></vaadin-icon
                    ></a>
                  </div>
                  <copilot-toggle-button
                    .ariaLabelledby="${e.id}-label"
                    .ariaDescribedby="${e.id}-desc"
                    ?checked=${e.enabled}
                    @on-change=${(t) => this.toggleFeatureFlag(t, e)}>
                  </copilot-toggle-button>
                </div>
              `
    )}
        </div>
        <div class="flex flex-col gap-1">
          ${a ? o`<h3 class="font-semibold my-0 text-sm">Copilot Experimental Features</h3>
                <div class="border-dashed flex flex-col divide-y">
                  ${M.filter((e) => e.available()).slice().sort((e, t) => e.name.localeCompare(t.name)).map(
      (e) => o`
                        <div class="flex gap-2 justify-between py-3.5">
                          <div class="flex flex-col">
                            <label id="${e.id}-label">${e.description}</label>
                          </div>
                          <copilot-toggle-button
                            .ariaLabelledby="${e.id}-label"
                            ?checked=${l.isExperimentalFeatureEnabled(e)}
                            @on-change=${(t) => this.toggleExperimentalFeatureFlag(t, e)}>
                          </copilot-toggle-button>
                        </div>
                      `
    )}
                </div>` : u}
        </div>
      </div>
    `;
  }
  toggleFeatureFlag(a, e) {
    const t = a.target.checked;
    v("use-feature", { source: "toggle", enabled: t, id: e.id }), g.frontendConnection ? (g.frontendConnection.send("setFeature", { featureId: e.id, enabled: t }), e.requiresServerRestart && i.toggleServerRequiringFeatureFlag(e), y({
      type: T.INFORMATION,
      message: `“${e.title}” ${t ? "enabled" : "disabled"}`,
      details: e.requiresServerRestart ? w() : void 0,
      dismissId: `feature${e.id}${t ? "Enabled" : "Disabled"}`
    }), I()) : g.log("error", `Unable to toggle feature ${e.title}: No server connection available`);
  }
  toggleExperimentalFeatureFlag(a, e) {
    const t = a.target.checked;
    v("use-experimental-feature", { source: "toggle", enabled: t, id: e.id });
    const r = l.isExperimentalFeatureEnabled(e);
    l.setExperimentalFeatureEnabled(e, t), e.requiresReload && t && !r && window.location.reload();
  }
};
d([
  h()
], m.prototype, "toggledFeaturesThatAreRequiresServerRestart", 2);
m = d([
  f("copilot-features-panel")
], m);
let b = class extends q {
  constructor() {
    super(...arguments), this.serverRestarting = !1;
  }
  createRenderRoot() {
    return this;
  }
  render() {
    if (i.serverRestartRequiringToggledFeatureFlags.length === 0)
      return u;
    if (!A())
      return u;
    const a = this.serverRestarting ? "Restarting..." : "Click to restart server";
    return o`
      <button
        ?disabled="${this.serverRestarting}"
        id="restart-server-btn"
        class="icon ${this.serverRestarting ? "" : "fade-in-out"}"
        @click=${() => {
      this.serverRestarting = !0, C();
    }}>
        <span>${x.refresh}</span>
      </button>
      <vaadin-tooltip for="restart-server-btn" text=${a}></vaadin-tooltip>
    `;
  }
};
d([
  h()
], b.prototype, "serverRestarting", 2);
b = d([
  f("copilot-features-actions")
], b);
const P = {
  header: "Features",
  tag: "copilot-features-panel",
  helpUrl: "https://vaadin.com/docs/latest/flow/configuration/feature-flags",
  actionsTag: "copilot-features-actions",
  toolbarOptions: {
    allowedModesWithOrder: {
      common: 0
    },
    iconKey: "listAlt"
  }
}, _ = {
  init(a) {
    a.addPanel(P);
  }
};
window.Vaadin.copilot.plugins.push(_);
export {
  b as CopilotFeaturesActions,
  m as CopilotFeaturesPanel
};
