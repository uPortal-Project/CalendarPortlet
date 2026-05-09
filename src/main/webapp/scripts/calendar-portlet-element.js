/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * <calendar-portlet> — narrow-view replacement for the Backbone+Underscore
 * implementation in scripts/calendar.js. Vanilla custom element, light DOM
 * (so existing Bootstrap + skin CSS continues to apply via document-level
 * selectors). Class names match the legacy markup so existing CSS and the
 * Playwright smoke tests in uPortal-start carry over without changes.
 *
 * Configuration: server-rendered URLs and date come in via attributes; i18n
 * strings come in via a single <script type="application/json"> child so
 * Spring's javaScriptEscape="true" handles each value cleanly.
 */
class CalendarPortlet extends HTMLElement {
    #days = 7;
    #startDate = null;
    #eventsUrl = null;
    #showDatePickerUrl = null;
    #hideDatePickerUrl = null;
    #viewMoreEventsUrl = null;
    #showDatePicker = false;
    #i18n = {};
    #lastFetched = { keys: [], dateMap: {}, dateNames: {} };

    connectedCallback() {
        this.#days = parseInt(this.getAttribute("days") || "7", 10);
        this.#startDate = this.getAttribute("start-date");
        this.#eventsUrl = this.getAttribute("events-url");
        this.#showDatePickerUrl = this.getAttribute("show-date-picker-url");
        this.#hideDatePickerUrl = this.getAttribute("hide-date-picker-url");
        this.#viewMoreEventsUrl = this.getAttribute("view-more-events-url");
        this.#showDatePicker = this.getAttribute("show-date-picker") === "true";

        const i18nNode = this.querySelector('script[type="application/json"].upcal-i18n');
        if (i18nNode) {
            try {
                this.#i18n = JSON.parse(i18nNode.textContent || "{}");
            } catch (err) {
                console.error("calendar-portlet: invalid i18n JSON", err);
            }
            i18nNode.remove();
        }

        this.#renderShell();
        this.#attachListeners();
        this.#fetchEvents();
    }

    #renderShell() {
        const t = this.#i18n;
        this.innerHTML = `
            <div class="upcal-event-view">
                <div class="row">
                    <div class="col-md-12">
                        <div class="row upcal-range">
                            <div class="col-md-6">
                                <h5>${this.#esc(t.view ?? "View")}</h5>
                                <div class="btn-group" role="group">
                                    <button type="button" data-days="1" class="btn btn-secondary upcal-range-day${this.#days === 1 ? " active" : ""}">${this.#esc(t.day ?? "Day")}</button>
                                    <button type="button" data-days="7" class="btn btn-secondary upcal-range-day${this.#days === 7 ? " active" : ""}">${this.#esc(t.week ?? "Week")}</button>
                                    <button type="button" data-days="31" class="btn btn-secondary upcal-range-day${this.#days === 31 ? " active" : ""}">${this.#esc(t.month ?? "Month")}</button>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <h5 class="text-end">${this.#esc(t.datePicker ?? "Date Picker")}</h5>
                                <div class="btn-group float-end" role="group">
                                    <button type="button" data-show="true" class="btn btn-secondary upcal-range-datepicker${this.#showDatePicker ? " active" : ""}">${this.#esc(t.show ?? "Show")}</button>
                                    <button type="button" data-show="false" class="btn btn-secondary upcal-range-datepicker${!this.#showDatePicker ? " active" : ""}">${this.#esc(t.hide ?? "Hide")}</button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="row upcal-inline-calendar-row" style="display:${this.#showDatePicker ? "" : "none"}">
                    <div class="col-md-12">
                        <div class="upcal-inline-calendar">
                            <input type="date" class="form-control upcal-date-input" value="${this.#esc(this.#dateInputValue())}">
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="col-md-12">
                        <div class="upcal-loading-message">
                            <p class="text-center"><i class="fa fa-spinner fa-spin"></i> ${this.#esc(t.loading ?? "Loading...")}</p>
                        </div>
                        <div class="alert alert-danger upcal-event-errors" style="display:none"></div>
                        <div class="upcal-event-list" style="display:none"></div>
                    </div>
                </div>
                ${this.#viewMoreEventsUrl ? `
                <div class="row">
                    <div class="col-md-12 upcal-view-links">
                        <a class="btn btn-link float-end upcal-view-more-events"
                           href="${this.#esc(this.#viewMoreEventsUrl)}"
                           title="${this.#esc(t.viewMoreEvents ?? "View more events")}">
                            ${this.#esc(t.viewMoreEvents ?? "View more events")} <i class="fa fa-arrow-right"></i>
                        </a>
                    </div>
                </div>` : ""}
            </div>
            <div class="upcal-event-details" style="display:none">
                <div class="row upcal-event-detail"></div>
                <div class="row utilities upcal-list-link">
                    <div class="col-md-12">
                        <a class="upcal-view-return" href="javascript:void(0)" data-role="button">
                            <i class="fa fa-arrow-left"></i> ${this.#esc(t.returnToCalendar ?? "Return to calendar")}
                        </a>
                    </div>
                </div>
            </div>
        `;
    }

    #attachListeners() {
        this.querySelectorAll(".upcal-range-day").forEach((btn) => {
            btn.addEventListener("click", () => {
                this.querySelectorAll(".upcal-range-day").forEach((b) => b.classList.remove("active"));
                btn.classList.add("active");
                this.#days = parseInt(btn.dataset.days, 10);
                this.#fetchEvents();
            });
        });

        this.querySelectorAll(".upcal-range-datepicker").forEach((btn) => {
            btn.addEventListener("click", () => {
                const show = btn.dataset.show === "true";
                this.querySelectorAll(".upcal-range-datepicker").forEach((b) => b.classList.remove("active"));
                btn.classList.add("active");
                this.#showDatePicker = show;
                const calRow = this.querySelector(".upcal-inline-calendar-row");
                if (calRow) calRow.style.display = show ? "" : "none";

                const url = show ? this.#showDatePickerUrl : this.#hideDatePickerUrl;
                if (url) {
                    fetch(url, { method: "POST" }).catch((err) =>
                        console.warn("calendar-portlet: failed to persist date-picker preference", err)
                    );
                }
            });
        });

        const dateInput = this.querySelector(".upcal-date-input");
        if (dateInput) {
            dateInput.addEventListener("change", (ev) => {
                const value = ev.target.value;
                if (!value) return;
                const [y, m, d] = value.split("-");
                this.#startDate = `${m}/${d}/${y}`;
                this.#fetchEvents();
            });
        }

        const detailReturn = this.querySelector(".upcal-event-details .upcal-view-return");
        if (detailReturn) {
            detailReturn.addEventListener("click", (ev) => {
                ev.preventDefault();
                this.#showListView();
            });
        }
    }

    async #fetchEvents() {
        const loading = this.querySelector(".upcal-loading-message");
        const list = this.querySelector(".upcal-event-list");
        const errs = this.querySelector(".upcal-event-errors");

        if (loading) loading.style.display = "";
        if (list) list.style.display = "none";
        if (errs) {
            errs.style.display = "none";
            errs.textContent = "";
        }

        const startToken = (this.#startDate || "").replace(/\//g, "");
        const url = (this.#eventsUrl || "")
            .replace(/START/, startToken)
            .replace(/DAYS/, String(this.#days));

        try {
            const response = await fetch(url, { credentials: "same-origin" });
            if (!response.ok) throw new Error(`Server returned ${response.status}`);
            const data = await response.json();

            if (Array.isArray(data?.errors) && data.errors.length > 0 && errs) {
                errs.innerHTML = "";
                data.errors.forEach((msg) => {
                    const p = document.createElement("p");
                    p.textContent = msg;
                    errs.appendChild(p);
                });
                errs.style.display = "";
            }

            this.#renderEventList(data);
        } catch (err) {
            console.error("calendar-portlet: fetch failed", err);
            if (errs) {
                errs.textContent = String(err.message || err);
                errs.style.display = "";
            }
        } finally {
            if (loading) loading.style.display = "none";
            if (list) list.style.display = "";
        }
    }

    #renderEventList(data) {
        const list = this.querySelector(".upcal-event-list");
        if (!list) return;

        const t = this.#i18n;
        const dateMap = data?.dateMap || {};
        const dateNames = data?.dateNames || {};
        const keys = Object.keys(dateMap);

        this.#lastFetched = { keys, dateMap, dateNames };

        if (keys.length === 0) {
            list.innerHTML = `
                <div class="row">
                    <div class="col-md-12 events-alert">
                        <div class="alert alert-warning">
                            <h4><i class="fa fa-exclamation-circle"></i> ${this.#esc(t.noEvents ?? "No events.")}</h4>
                        </div>
                    </div>
                </div>
            `;
            return;
        }

        const html = keys
            .map((key) => {
                const wrappers = dateMap[key] || [];
                const events = wrappers
                    .map((w) => this.#renderEvent(w?.event ?? {}, w?.colorIndex ?? 0))
                    .join("");
                return `
                    <div class="row day">
                        <div class="col-md-12">
                            <h4>${this.#esc(dateNames[key] ?? key)}</h4>
                            ${events}
                        </div>
                    </div>
                `;
            })
            .join("");

        list.innerHTML = html;

        list.querySelectorAll(".day").forEach((dayEl, dayIndex) => {
            dayEl.querySelectorAll(".upcal-event-wrapper").forEach((wrapperEl, eventIndex) => {
                const link = wrapperEl.querySelector(".upcal-event-title a");
                if (!link) return;
                link.addEventListener("click", (ev) => {
                    ev.preventDefault();
                    const dayKey = this.#lastFetched.keys[dayIndex];
                    const wrappers = this.#lastFetched.dateMap[dayKey] || [];
                    const evt = wrappers[eventIndex]?.event;
                    if (evt) this.#showEventDetail(evt);
                });
            });
        });
    }

    #renderEvent(event, colorIndex) {
        const t = this.#i18n;
        let timeDisplay;
        if (event.allDay) {
            timeDisplay = this.#esc(t.allDay ?? "All day");
        } else if (event.multiDay) {
            timeDisplay = `${this.#esc(event.dateStartTime ?? "")} - ${this.#esc(event.dateEndTime ?? "")}`;
        } else if (
            event.endTime &&
            (event.endTime !== event.startTime || event.startDate !== event.endDate)
        ) {
            timeDisplay = `${this.#esc(event.startTime ?? "")} - ${this.#esc(event.endTime ?? "")}`;
        } else {
            timeDisplay = this.#esc(event.startTime ?? "");
        }

        return `
            <div class="upcal-event-wrapper">
                <div class="upcal-event upcal-color-${this.#esc(String(colorIndex ?? 0))}">
                    <div class="upcal-event-cal"><span></span></div>
                    <span><strong>${timeDisplay}</strong></span>
                    <h5 class="upcal-event-title"><a href="#">${this.#esc(event.summary ?? "")}</a></h5>
                </div>
            </div>
        `;
    }

    #showEventDetail(event) {
        const view = this.querySelector(".upcal-event-view");
        const details = this.querySelector(".upcal-event-details");
        const detailContent = this.querySelector(".upcal-event-detail");
        if (!view || !details || !detailContent) return;

        const t = this.#i18n;
        const e = event;

        let timeDisplay;
        if (e.multiDay) {
            timeDisplay = `${this.#esc(e.startTime ?? "")} ${this.#esc(e.startDate ?? "")} - ${this.#esc(e.endTime ?? "")} ${this.#esc(e.endDate ?? "")}`;
        } else if (e.allDay) {
            timeDisplay = `${this.#esc(t.allDay ?? "All day")} ${this.#esc(e.startDate ?? "")}`;
        } else if (
            e.endTime &&
            (e.endTime !== e.startTime || e.startDate !== e.endDate)
        ) {
            timeDisplay = `${this.#esc(e.startTime ?? "")} ${this.#esc(e.endTime ?? "")} ${this.#esc(e.startDate ?? "")}`;
        } else {
            timeDisplay = `${this.#esc(e.startTime ?? "")} ${this.#esc(e.startDate ?? "")}`;
        }

        const sections = [];
        sections.push(`<div class="col-md-12"><h4>${this.#esc(e.summary ?? "")}</h4></div>`);
        sections.push(`
            <div class="col-md-12">
                <div class="upcal-event-wrapper">
                    <span><strong>${timeDisplay}</strong></span>
                </div>
            </div>
        `);
        if (e.location) {
            sections.push(`
                <div class="col-md-12">
                    <h5>${this.#esc(t.location ?? "Location")}:</h5>
                    <p>${this.#esc(e.location)}</p>
                </div>
            `);
        }
        if (e.description) {
            sections.push(`
                <div class="col-md-12">
                    <h5>${this.#esc(t.description ?? "Description")}:</h5>
                    <p>${this.#esc(e.description)}</p>
                </div>
            `);
        }
        if (e.link) {
            sections.push(`
                <div class="col-md-12">
                    <h5>${this.#esc(t.link ?? "Link")}:</h5>
                    <p><a href="${this.#esc(e.link)}" target="_blank" rel="noopener noreferrer">${this.#esc(e.link)}</a></p>
                </div>
            `);
        }

        detailContent.innerHTML = sections.join("");
        view.style.display = "none";
        details.style.display = "";
    }

    #showListView() {
        const view = this.querySelector(".upcal-event-view");
        const details = this.querySelector(".upcal-event-details");
        if (view) view.style.display = "";
        if (details) details.style.display = "none";
    }

    #dateInputValue() {
        if (!this.#startDate) return "";
        const parts = this.#startDate.split("/");
        if (parts.length !== 3) return "";
        const [m, d, y] = parts;
        return `${y}-${m.padStart(2, "0")}-${d.padStart(2, "0")}`;
    }

    #esc(str) {
        const div = document.createElement("div");
        div.textContent = String(str ?? "");
        return div.innerHTML;
    }
}

if (!customElements.get("calendar-portlet")) {
    customElements.define("calendar-portlet", CalendarPortlet);
}
