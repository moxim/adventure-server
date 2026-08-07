import { n as e } from "./chunk-DiqZc92J.js";
import { $ as t, L as n, R as r, at as i, et as a, n as o, o as s, r as c, t as l, u, ut as d } from "./icons-Daxdm7Mp.js";
import { l as f, o as p } from "./consts-PqkJ5Usg.js";
import { a as m, d as h, i as g, l as _, o as v, r as y, s as b } from "./section-panel-ui-state-ByeyD6ou.js";
import { n as x, r as S } from "./copilot-ui-state-DjlBBpyf.js";
import { a as C, c as w, i as T, l as E, o as D, u as O } from "./copilot-error-handler-B7az3wX2.js";
import { n as k, t as A } from "./copilot-stored-machine-state-DS3t0BPl.js";
import { n as j, r as M } from "./copilot-notification-BnxWYvla.js";
import { n as N, t as P } from "./base-panel-Cq1wQn0M.js";
import { n as F, t as I } from "./copilot-message-box-DbAltoiu.js";
//#region frontend/copilot/plugins/copilot-vaadin-versions/vaadin-version-request.ts
function L() {
	x.setVaadinVersionState({ loading: !0 }), r(`${p}get-new-vaadin-versions`, { includePreReleases: k.getNewVersionPreReleasesVisible() }, (e) => {
		let t = e.data;
		if (t.error) {
			x.setVaadinVersionState({
				loading: !1,
				errorMessage: t.error.message,
				hasError: !0,
				versions: []
			});
			return;
		}
		if (!t.newVersions) return;
		let n = JSON.parse(t.newVersions);
		x.setVaadinVersionState({
			versions: n,
			loading: !1,
			hasError: !1
		});
	});
}
var R = e((() => {
	n(), f(), A(), S(), i(), d(() => k.getNewVersionPreReleasesVisible(), () => {
		L();
	}, { fireImmediately: !0 });
})), z, B, V;
//#endregion
e((() => {
	b(), a(), c(), N(), S(), g(), o(), A(), n(), f(), j(), I(), R(), D(), w(), v(), z = class extends P {
		constructor(...e) {
			super(...e), this.renderIcon = (e) => e.name === "Flow" ? l.flow : e.name === "Hilla" ? l.hilla : e.name === "Web Components" || e.name === "Flow Components" ? l.uiComponents : e.name === "TestBench" ? l.checklist : e.name.indexOf("MPR") === -1 ? e.name.indexOf("AppSec") === -1 ? e.name.indexOf("Collaboration") === -1 ? e.name === "Copilot" || e.name === "CoPilot" ? l.copilot : e.name.indexOf("Kubernetes") === -1 ? e.name.indexOf("Observability") === -1 ? e.name.indexOf("SSO") === -1 ? e.name.indexOf("Modernization") === -1 ? l.code : l.rocketLaunch : l.login : l.visibility : l.kubernetes : l.group : l.verifiedUser : l.playCircle;
		}
		render() {
			return u`
      <ul class="list-none m-0 pb-2.5 px-2">
        ${this.renderContent()}
      </ul>
    `;
		}
		renderContent() {
			return x.newVaadinVersionState === void 0 || x.newVaadinVersionState.loading ? F("loading", "Versions are loading...") : x.newVaadinVersionState.hasError ? F("error", x.newVaadinVersionState.errorMessage ?? "Unable to display new versions") : !x.newVaadinVersionState.versions || x.newVaadinVersionState.versions.length === 0 ? F("success", "Vaadin version is up to date") : u`
      ${x.newVaadinVersionState.versions.map((e, t) => this.renderNewVersionItem(e, t === 0))}
    `;
		}
		renderNewVersionItem(e, t) {
			return u`
      <li>
        <vaadin-details ?opened="${t}">
          <vaadin-details-summary class="relative" slot="summary">
            <div class="flex gap-2 items-center">
              ${e.version}
              ${t ? u`<span
                    class="bg-blue-3 dark:bg-blue-7 font-normal inline-flex px-1.5 py-px rounded-full text-xs text-blue-11 dark:text-blue-12"
                    >Latest</span
                  >` : s}
              <vaadin-button
                aria-label="Update"
                class="absolute end-0 top-0"
                theme="icon tertiary"
                ?disabled="${this.updateClickedVersion !== void 0}"
                @click="${(t) => {
				t.stopPropagation(), this.sendUpdateRequest(e.version, e.preRelease);
			}}">
                <vaadin-icon
                  class="${this.updateClickedVersion === e.version ? "animate-spin" : ""}"
                  .svg="${this.updateClickedVersion === e.version ? l.progressActivity : l.upgrade}"></vaadin-icon>
                <vaadin-tooltip slot="tooltip" text="Update"></vaadin-tooltip>
              </vaadin-button>
            </div>
          </vaadin-details-summary>
          <ul class="border-dashed divide-y list-none m-0 pe-2 ps-8">
            ${e.changelogs.map((e) => u`
                <li class="flex gap-2 py-2">
                  <vaadin-icon .svg="${this.renderIcon(e)}"></vaadin-icon>
                  ${e.name}
                  <a href="${e.url}" target="_blank">${e.version}</a>
                </li>
              `)}
          </ul>
        </vaadin-details>
      </li>
    `;
		}
		sendUpdateRequest(e, n) {
			this.updateClickedVersion = e;
			let i = {
				newVersion: e,
				preRelease: n
			};
			r(`${p}update-vaadin-version`, i, (e) => {
				this.updateClickedVersion = void 0;
				let n = !C(e.data, i);
				if (n) {
					let e = "Please restart the server";
					O() && (e = "Server will be restarted by the IDE plugin", E()), M({
						message: "Version updated",
						type: t.INFORMATION,
						details: e
					});
				}
				return n;
			}).catch((e) => {
				T("Error updating version", e);
			});
		}
	}, m([_()], z.prototype, "updateClickedVersion", void 0), z = m([h("copilot-vaadin-versions")], z), B = class extends y {
		createRenderRoot() {
			return this;
		}
		render() {
			let e = k.getNewVersionPreReleasesVisible();
			return u`<vaadin-button
      aria-pressed="${e}"
      theme="tertiary"
      @click="${(e) => {
				e.stopPropagation(), k.setNewVersionPreReleasesVisible(!k.getNewVersionPreReleasesVisible());
			}}">
      <vaadin-icon slot="prefix" .svg="${e ? l.visibility : l.visibilityOff}"></vaadin-icon>
      Prereleases
    </vaadin-button>`;
		}
	}, B = m([h("copilot-vaadin-versions-actions")], B), V = {
		header: "Vaadin Versions",
		tag: "copilot-vaadin-versions",
		actionsTag: "copilot-vaadin-versions-actions"
	}, window.Vaadin.copilot.plugins.push({ init(e) {
		e.addPanel(V);
	} });
}))();
export { z as CopilotVaadinVersions, B as CopilotVersionCheckerActions, V as versionCheckerPanel };
