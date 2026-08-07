import { n as e } from "./chunk-DiqZc92J.js";
import { _ as t, g as n, n as r, r as i, t as a, u as o } from "./icons-Daxdm7Mp.js";
import { l as s, o as c } from "./consts-PqkJ5Usg.js";
import { a as l, c as u, d, l as f, n as p, o as m, s as h, t as g } from "./section-panel-ui-state-ByeyD6ou.js";
import { n as _, t as v } from "./copilot-eventbus-tR06exIW.js";
import { n as y, r as b } from "./copilot-ui-state-DjlBBpyf.js";
import { r as x, t as S } from "./stats-D_j8yo6z.js";
import { n as C, t as w } from "./base-panel-Cq1wQn0M.js";
//#region frontend/copilot/plugins/copilot-feedback/copilot-feedback-plugin.css?inline
var T, E = e((() => {
	T = "copilot-feedback-panel{font:var(--copilot-font-xs);gap:var(--space-200);padding:var(--space-150);flex-direction:column;display:flex}copilot-feedback-panel>p{margin:0}copilot-feedback-panel .dialog-footer{gap:var(--space-100);display:flex}copilot-feedback-panel :is(vaadin-select,vaadin-email-field)::part(input-field){padding-block:0}copilot-feedback-panel :is(vaadin-select)::part(input-field){padding-inline-end:0}copilot-feedback-panel vaadin-select::part(toggle-button){height:var(--copilot-size-md);width:var(--copilot-size-md);justify-content:center;align-items:center;display:flex}copilot-feedback-panel vaadin-text-area textarea{line-height:var(--copilot-line-height-sm)}copilot-feedback-panel vaadin-text-area:hover::part(input-field){background:0 0}copilot-feedback-panel>*::part(helper-text){line-height:var(--copilot-line-height-sm);margin:0}";
})), D, O, k, A, j, M, N;
//#endregion
e((() => {
	h(), C(), i(), E(), n(), s(), _(), r(), g(), S(), b(), m(), D = "https://github.com/vaadin", O = "https://github.com/vaadin/copilot/issues/new", k = "?template=feature_request.md&title=%5BFEATURE%5D", A = "A short, concise description of the bug and why you consider it a bug. Any details like exceptions and logs can be helpful as well.", j = "Please provide as many details as possible, this will help us deliver a fix as soon as possible.%0AThank you!%0A%0A%23%23%23 Description of the Bug%0A%0A{description}%0A%0A%23%23%23 Expected Behavior%0A%0AA description of what you would expect to happen. (Sometimes it is clear what the expected outcome is if something does not work, other times, it is not super clear.)%0A%0A%23%23%23 Minimal Reproducible Example%0A%0AWe would appreciate the minimum code with which we can reproduce the issue.%0A%0A%23%23%23 Versions%0A{versionsInfo}", M = class extends w {
		constructor() {
			super(), this.description = "", this.types = [
				{
					label: "Generic feedback",
					value: "feedback",
					ghTitle: ""
				},
				{
					label: "Report a bug",
					value: "bug",
					ghTitle: "[BUG]"
				},
				{
					label: "Ask a question",
					value: "question",
					ghTitle: "[QUESTION]"
				},
				{
					label: "Share an idea",
					value: "idea",
					ghTitle: "[FEATURE]"
				}
			], this.type = this.types[0].value, this.topics = [
				{
					label: "Generic",
					value: "platform"
				},
				{
					label: "Flow",
					value: "flow"
				},
				{
					label: "Hilla",
					value: "hilla"
				},
				{
					label: "Copilot",
					value: "copilot"
				}
			], this.topic = this.topics[0].value;
		}
		render() {
			return o`<style>
        ${T}</style
      >${this.renderContent()}${this.renderFooter()}`;
		}
		renderContent() {
			return this.message === void 0 ? o`
          <p>
            Your insights are incredibly valuable to us. Whether you’ve encountered a hiccup, have questions, or ideas
            to make our platform better, we're all ears! If you wish, leave your email, and we’ll get back to you. You
            can even share your code snippet with us for a clearer picture.
          </p>
          <vaadin-select
            .items="${this.types}"
            .value="${this.type}"
            overlay-class="alwaysVisible"
            @value-changed=${(e) => {
				this.type = e.detail.value;
			}}>
          </vaadin-select>
          <vaadin-select
            label="Feedback Topic"
            overlay-class="alwaysVisible"
            .items=${this.topics}
            .value="${this.topic}"
            .hidden=${this.type !== "feedback"}
            @value-changed=${(e) => {
				this.topic = e.detail.value;
			}}>
          </vaadin-select>
          <vaadin-text-area
            .value="${this.description}"
            @keydown=${this.keyDown}
            @focus=${() => {
				this.descriptionField.invalid = !1, this.descriptionField.placeholder = "";
			}}
            @value-changed=${(e) => {
				this.description = e.detail.value;
			}}
            label="Tell Us More"
            helper-text="Describe what you're experiencing, wondering about, or envisioning. The more you share, the better we can understand and act on your feedback"></vaadin-text-area>
          <vaadin-text-field
            @keydown=${this.keyDown}
            @value-changed=${(e) => {
				this.email = e.detail.value;
			}}
            .required=${this.type === "question"}
            id="email"
            value="${y.userInfo?.email}"
            label="Your Email${this.type === "question" ? "" : " (Optional)"}"
            helper-text="Leave your email if you’d like us to follow up, we’d love to keep the conversation going."></vaadin-text-field>
        ` : o`<p>${this.message}</p>`;
		}
		renderFooter() {
			return this.message === void 0 ? o`
          <div class="dialog-footer">
            <button
              style="margin-inline-end: auto"
              @click="${() => {
				v.emit("system-info-with-callback", {
					callback: (e) => this.openGithub(e, this),
					notify: !1
				});
			}}">
              <span class="prefix">${a.github}</span>
              Create GitHub Issue
            </button>
            <button @click="${this.close}">Cancel</button>
            <button class="primary" @click="${this.submit}" .disabled=${this.type === "question" && !this.email}>
              Submit
            </button>
          </div>
        ` : o` <div class="footer">
          <vaadin-button @click="${this.close}">Close</vaadin-button>
        </div>`;
		}
		close() {
			p.closePanel("copilot-feedback-panel");
		}
		submit() {
			if (x("feedback", {
				github: !1,
				type: this.type,
				topic: this.topic
			}), this.description.trim() === "") {
				this.descriptionField.invalid = !0, this.descriptionField.placeholder = "Please tell us more before sending", this.descriptionField.value = "";
				return;
			}
			let e = {
				description: this.description,
				email: this.email,
				type: this.type,
				topic: this.topic
			};
			v.emit("system-info-with-callback", {
				callback: (n) => t(`${c}feedback`, {
					...e,
					versions: n
				}),
				notify: !1
			}), this.parentNode?.style.setProperty("--section-height", "150px"), this.message = "Thank you for sharing feedback.";
		}
		keyDown(e) {
			(e.key === "Backspace" || e.key === "Delete") && e.stopPropagation();
		}
		openGithub(e, t) {
			if (x("feedback", {
				github: !0,
				type: this.type,
				topic: this.topic
			}), this.type === "idea") {
				window.open(`${O}${k}`);
				return;
			}
			if (this.type === "feedback") {
				window.open(`${D}/${this.topic}/issues/new`);
				return;
			}
			let n = e ? e.replace(/\n/g, "%0A") : "Activate Copilot to include version info.", r = `${t.types.find((e) => e.value === this.type)?.ghTitle}`, i = t.description === "" ? A : t.description, a = j.replace("{description}", i).replace("{versionsInfo}", n);
			window.open(`${O}?title=${r}&body=${a}`, "_blank")?.focus();
		}
	}, l([f()], M.prototype, "description", void 0), l([f()], M.prototype, "type", void 0), l([f()], M.prototype, "topic", void 0), l([f()], M.prototype, "email", void 0), l([f()], M.prototype, "message", void 0), l([f()], M.prototype, "types", void 0), l([f()], M.prototype, "topics", void 0), l([u("vaadin-text-area")], M.prototype, "descriptionField", void 0), M = l([d("copilot-feedback-panel")], M), N = {
		header: "Help Us Improve!",
		tag: "copilot-feedback-panel",
		individual: !0
	}, window.Vaadin.copilot.plugins.push({ init(e) {
		e.addPanel(N);
	} }), p.addPanel(N);
}))();
export { M as CopilotFeedbackPanel };
