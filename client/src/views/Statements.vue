<template>
  <div>
    <breadcrumbs :breadcrumbs="[{ name: 'Statements', path: '/statements' }]"></breadcrumbs>

    <div>
      <h1 class="text-2xl font-semibold mb-4">Range</h1>

      <vue-tailwind-datepicker :shortcuts="dateShortcuts" :formatter="dateFormatter" v-model="range" />

      <div class="p-4 mt-4 text-sm text-yellow-700 bg-yellow-100 rounded-lg" role="alert">
        Budget limits are calculated proportionally to the days/months selected.
      </div>
    </div>

    <h1 class="mt-8 text-2xl font-semibold">Filters</h1>

    <div class="mt-4">
      <h1 class="text-xl font-semibold mb-4"><font-awesome-icon icon="fa-solid fa-coins"></font-awesome-icon> Categories & Budgets</h1>

      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-4">
        <Category v-for="category in categories"
                  v-bind:key="'category-' + category.id"
                  class="cursor-pointer hover:bg-gray-100"
                  :style="{ opacity: isCategoryFilterActive(category.id) ? 1 : 0.5 }"
                  :category="category"
                  :statements="getCategoryStatements(category)"
                  :budgetFactor="budgetFactor"
                  @click="toggleCategoryFilter(category.id)"></Category>
      </div>
    </div>

    <div class="mt-4" v-if="tags.length > 0">
      <h1 class="text-xl font-semibold mb-4"><font-awesome-icon icon="fa-solid fa-tag"></font-awesome-icon> Tags</h1>

      <div>
        <div class="inline-block rounded-full py-0.5 px-2.5 cursor-pointer opacity-50 hover:opacity-100 mr-1 mb-1.5 bg-red-200"
             @click="clearTagFilters">
          <font-awesome-icon icon="fa-solid fa-xmark"></font-awesome-icon>
        </div>

        <div v-for="tag in tags" v-bind:key="'tag-' + tag.id"
             class="inline-block rounded-full py-0.5 px-2.5 cursor-pointer hover:opacity-100 mr-1 mb-1.5 border"
             :class="{ 'opacity-50 border-white': !isTagFilterActive(tag.id), 'opacity-80 border-black': isTagFilterActive(tag.id) }"
             :style="{ 'background-color': tag.attributes.color, color: tag.attributes.textColor }"
             @click="toggleTagFilter(tag.id)">
          <span>{{ tag.attributes.name }}</span>
        </div>
      </div>
    </div>

    <div class="mt-4" v-if="accounts.length > 0">
      <h1 class="text-xl font-semibold mb-4"><font-awesome-icon icon="fa-solid fa-wallet"></font-awesome-icon> Accounts</h1>

      <div>
        <div class="inline-block rounded-full py-0.5 px-2.5 cursor-pointer opacity-50 hover:opacity-100 mr-1 mb-1.5 bg-red-200"
             @click="clearAccountFilters">
          <font-awesome-icon icon="fa-solid fa-xmark"></font-awesome-icon>
        </div>

        <div v-for="account in accounts" v-bind:key="'account-' + account.id"
             class="inline-block rounded-full py-0.5 px-2.5 cursor-pointer hover:opacity-100 mr-1 mb-1.5 bg-gray-100 text-black border"
             :class="{ 'opacity-50 border-white': !isAccountFilterActive(account.id), 'opacity-80 border-black': isAccountFilterActive(account.id) }"
             @click="toggleAccountFilter(account.id)">
          <span>{{ account.attributes.name }}</span>
        </div>
      </div>
    </div>

    <div class="mt-4">
      <input type="text" placeholder="Search" class="w-full rounded-md" v-model="term" />
    </div>

    <div class="mt-8 mb-8">
      <div class="flex">
        <h1 class="text-2xl font-semibold mb-4 flex-grow">Statements</h1>

        <div>
          <div class="inline-block rounded-full py-0.5 px-2.5 cursor-pointer opacity-50 hover:opacity-100 mr-1 mb-1.5 w-12 text-center"
               :class="{ 'bg-red-200': showInactive, 'bg-green-200': !showInactive }"
               :title="showInactive ? 'Hide inactive statements' : 'Show inactive statements'"
               @click="showInactive = !showInactive">
            <font-awesome-icon v-if="!showInactive" icon="fa-solid fa-eye"></font-awesome-icon>
            <font-awesome-icon v-if="showInactive" icon="fa-solid fa-eye-slash"></font-awesome-icon>
          </div>
        </div>
      </div>

      <div class="grid grid-cols-1 gap-4">
        <div class="block p-6 bg-gray-800 rounded-lg shadow grid grid-cols-2 gap-4 text-white">
          <div class="flex items-center">
            <h1 class="text-lg">
              <font-awesome-icon icon="fa-solid fa-coins"></font-awesome-icon>
              <span class="ml-2.5">Change in Balance</span>
            </h1>
          </div>
          <div class="text-right">
            <div>
              <span>Total: </span>
              <span class="text-lg font-semibold" :class="{ 'text-red-500': total < 0, 'text-green-500': total >= 0 }">
                {{ total >=0 ? '+' : '' }}{{ $filters.formatNumber(total) }}
              </span>
            </div>
            <div v-if="filteredStatements.length !== statements.length" >
              <span>Filtered: </span>
              <span v-if="filteredStatements.length !== statements.length" class="text-lg font-semibold" :class="{ 'text-red-500': filteredTotal < 0, 'text-green-500': filteredTotal >= 0 }">
                {{ filteredTotal >= 0 ? '+' : '' }}{{ $filters.formatNumber(filteredTotal) }}
              </span>
            </div>
          </div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
          <div class="block p-3 bg-green-500 hover:bg-green-600 text-white rounded-lg shadow-md text-center text-lg cursor-pointer"
               @click="showModal(null)">
            <font-awesome-icon icon="fa-solid fa-circle-plus"></font-awesome-icon>
            <span class="ml-2.5">Add Statement</span>
          </div>

          <div class="block p-3 bg-green-500 hover:bg-green-600 text-white rounded-lg shadow-md text-center text-lg cursor-pointer"
               @click="showImportModal = true">
            <font-awesome-icon icon="fa-solid fa-file-import"></font-awesome-icon>
            <span class="ml-2.5">Import Statements</span>
          </div>

          <div class="block p-3 bg-blue-300 hover:bg-blue-400 text-white rounded-lg shadow-md text-center text-lg cursor-pointer"
               @click="applyPatterns">
            <font-awesome-icon icon="fa-solid fa-arrows-rotate"></font-awesome-icon>
            <span class="ml-2.5">Apply Categories & Tags</span>
          </div>

          <div class="block p-3 bg-red-500 hover:bg-red-600 text-white rounded-lg shadow-md text-center text-lg cursor-pointer"
               @click="deleteStatements">
            <font-awesome-icon icon="fa-solid fa-trash"></font-awesome-icon>
            <span class="ml-2.5">Delete All Statements</span>
          </div>
        </div>

        <div class="text-center flex gap-4 items-center" v-if="statements.length !== filteredStatements.length">
          <div class="flex-grow bg-gray-300 h-0.5 rounded-xl"></div>
          <div>{{ statements.length - filteredStatements.length }} statements are hidden based on the filters selected</div>
          <div class="flex-grow bg-gray-300 h-0.5 rounded-xl"></div>
        </div>

        <div v-for="(entry, index) in entries" v-bind:key="'entry-' + index">
          <div v-if="entry.type === 'transfer'">
            <div class="block p-6 bg-indigo-50 border border-indigo-100 rounded-lg shadow-md text-sm hover:bg-indigo-100 cursor-pointer"
                 @click="toggleTransfer(entry)">
              <div class="items-center grid grid-cols-5 gap-4">
                <div>
                  <div class="text-left">
                    <div :class="{
                      'text-lg font-semibold hover:opacity-100': true,
                      'text-red-500': entry.first.attributes.amount < 0,
                      'opacity-50': entry.first.attributes.valueDate > new Date().toISOString() }">
                      {{ $filters.formatNumber(entry.first.attributes.amount) }} {{ entry.first.attributes.currency }}
                    </div>
                    <div>{{ entry.first.attributes.type }}</div>
                  </div>
                </div>

                <div class="text-center">
                  <div>{{ entry.firstAccount.attributes.name }}</div>
                  <div>{{ entry.first.attributes.valueDate }}</div>
                </div>

                <div class="text-center">
                  <font-awesome-icon icon="fa-solid fa-angles-right"></font-awesome-icon>
                </div>

                <div class="text-center">
                  <div>{{ entry.secondAccount.attributes.name }}</div>
                  <div>{{ entry.second.attributes.valueDate }}</div>
                </div>

                <div>
                  <div class="text-right">
                    <div :class="{
                      'text-lg font-semibold hover:opacity-100': true,
                      'text-red-500': entry.second.attributes.amount < 0,
                      'opacity-50': entry.second.attributes.valueDate > new Date().toISOString() }">
                      {{ $filters.formatNumber(entry.second.attributes.amount) }} {{ entry.second.attributes.currency }}
                    </div>
                    <div>{{ entry.second.attributes.type }}</div>
                  </div>
                </div>
              </div>

              <div v-if="isExpanded(entry)" class="mt-4">
                <Statement class="hover:bg-gray-200 cursor-pointer p-4"
                           :statement="entry.first"
                           @click.stop="showModal(entry.first)"></Statement>

                <Statement class="hover:bg-gray-200 cursor-pointer p-4 mt-2"
                           :statement="entry.second"
                           @click.stop="showModal(entry.second)"></Statement>
              </div>
            </div>
          </div>

          <Statement v-if="entry.type === 'statement'"
                     class="hover:bg-gray-100 cursor-pointer"
                     :statement="entry.statement"
                     @click="showModal(entry.statement)"></Statement>
        </div>
      </div>
    </div>

    <modal :title="selectedStatement.id ? 'Edit Statement' : 'Add Statement'" v-if="selectedStatement" @hide="hideModal">
      <div class="grid grid-cols-3 gap-4">
        <div>
          <div class="font-semibold">Amount</div>
          <input type="number" placeholder="Amount"
                 class="rounded-md w-full"
                 :class="{ 'border-red-500': selectedStatement.attributes.amount === '' }"
                 v-model="selectedStatement.attributes.amount">
        </div>
        <div>
          <div class="font-semibold">Currency</div>
          <input type="text" min="3" max="3"
                 class="rounded-md w-full"
                 placeholder="Currency"
                 :class="{ 'border-red-500': selectedStatement.attributes.currency.length !== 3 }"
                 v-model="selectedStatement.attributes.currency">
        </div>
        <div>
          <div class="font-semibold">Type</div>
          <input type="text" placeholder="Type"
                 class="rounded-md w-full"
                 :class="{ 'border-red-500': !selectedStatement.attributes.type }"
                 v-model="selectedStatement.attributes.type">
        </div>
      </div>

      <div class="grid grid-cols-2 gap-4">
        <div>
          <div class="font-semibold">Booking Date</div>
          <input type="date" placeholder="Booking Date"
                 class="rounded-md w-full"
                 :class="{ 'border-red-500': !selectedStatement.attributes.bookingDate }"
                 v-model="selectedStatement.attributes.bookingDate">
        </div>
        <div>
          <div class="font-semibold">Value Date</div>
          <input type="date" placeholder="Value Date"
                 class="rounded-md w-full"
                 :class="{ 'border-red-500': !selectedStatement.attributes.valueDate }"
                 v-model="selectedStatement.attributes.valueDate">
        </div>
      </div>

      <div class="mt-2">
        <div class="font-semibold">Purpose</div>
        <textarea type="text" class="rounded-md w-full" placeholder="Purpose" v-model="selectedStatement.attributes.purpose"></textarea>
      </div>


      <div class="mt-2 grid grid-cols-3 gap-4">
        <div>
          <div class="font-semibold">Third Party Name</div>
          <input type="text" class="rounded-md w-full" placeholder="Third Party Name" v-model="selectedStatement.attributes.thirdPartyName">
        </div>
        <div>
          <div class="font-semibold">Third Party Account</div>
          <input type="text" class="rounded-md w-full" placeholder="Third Party Account" v-model="selectedStatement.attributes.thirdPartyAccount">
        </div>
        <div>
          <div class="font-semibold">Third Party Bank Code</div>
          <input type="text" class="rounded-md w-full" placeholder="Third Party Bank Code" v-model="selectedStatement.attributes.thirdPartyBankCode">
        </div>
      </div>

      <div class="mt-2 grid grid-cols-2 gap-4">
        <div>
          <div class="font-semibold">Creditor ID</div>
          <input type="text" class="rounded-md w-full" placeholder="Creditor ID" v-model="selectedStatement.attributes.creditorId">
        </div>
        <div>
          <div class="font-semibold">Mandate Reference</div>
          <input type="text" class="rounded-md w-full" placeholder="Mandate Reference" v-model="selectedStatement.attributes.mandateReference">
        </div>
      </div>

      <div class="mt-2 grid grid-cols-2 gap-4">
        <div>
          <div class="font-semibold">Payment Information ID</div>
          <input type="text" class="rounded-md w-full" placeholder="Payment Information ID" v-model="selectedStatement.attributes.paymentInformationId">
        </div>
        <div>
          <div class="font-semibold">Customer Reference</div>
          <input type="text" class="rounded-md w-full" placeholder="Payment Information ID" v-model="selectedStatement.attributes.customerReference">
        </div>
      </div>

      <div class="mt-2">
        <div class="font-semibold">Comment</div>
        <textarea class="rounded-md w-full" placeholder="Comment" v-model="selectedStatement.attributes.comment"></textarea>
      </div>

      <div class="mt-2 grid grid-cols-3 gap-4">
        <div>
          <div class="font-semibold">Account</div>
          <select class="w-full rounded-md" :value="selectedStatement.relationships.account.data.id" @change="e => selectedStatement.relationships.account.data.id = e.target.value">
            <option v-for="account in accounts" :value="account.id">{{ account.attributes.name }}</option>
          </select>
        </div>
        <div>
          <div class="font-semibold">Category</div>
          <select class="w-full rounded-md" :value="selectedStatement.relationships.category.data.id" @change="e => selectedStatement.relationships.category.data.id = e.target.value">
            <option :value="-1">Select Automatically</option>
            <option v-for="category in categories" :value="category.id">{{ category.attributes.name }}</option>
          </select>
        </div>
        <div>
          <div class="font-semibold">State</div>
          <select class="w-full rounded-md" :value="selectedStatement.attributes.state" @change="e => selectedStatement.attributes.state = e.target.value">
            <option value="ACTIVE">Active</option>
            <option value="INACTIVE">Inactive</option>
          </select>
        </div>
      </div>

      <div class="mt-2">
        <div class="font-semibold">Tags</div>
        <div class="form-check">
          <input v-model="autoSelectStatementTags"
                 class="form-check-input appearance-none h-4 w-4 border border-gray-300 rounded-sm bg-white checked:bg-blue-600 checked:border-blue-600 focus:outline-none transition duration-200 mt-1 align-top bg-no-repeat bg-center bg-contain float-left mr-2 cursor-pointer"
                 type="checkbox"
                 id="flexCheckDefault">
          <label class="form-check-label inline-block text-gray-800" for="flexCheckDefault">
            Apply tags automatically
          </label>
        </div>
        <select v-show="tags.length > 0 && !autoSelectStatementTags" class="rounded-md w-full mt-2" multiple ref="tagSelector">
          <option v-for="tag in tags" v-bind:key="'tag-option-' + tag.id" :value="tag.id" :selected="isTagActive(selectedStatement, tag.id)">{{ tag.attributes.name }}</option>
        </select>
      </div>

      <div class="mt-4 pt-4 border-t-2 flex gap-2 flex">
        <input type="submit" value="Delete"
               v-if="selectedStatement.id"
               class="rounded-md bg-red-500 px-4 py-1.5 text-white cursor-pointer hover:bg-red-800"
               @click="deleteStatement">
        <div class="flex-grow"></div>
        <input type="submit" value="Save"
               class="rounded-md bg-blue-500 px-4 py-1.5 text-white cursor-pointer hover:bg-blue-800"
               @click="submitStatement">
      </div>
    </modal>

    <StatementImport :show="showImportModal" @hide="showImportModal = false" @imported="refreshEverything"></StatementImport>
  </div>
</template>

<script>
import dayjs from "dayjs";
import Category from "../components/Category.vue";
import Breadcrumbs from "../components/Breadcrumbs.vue";
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import Statement from "../components/Statement.vue"
import Card from "../components/Card.vue";
import Modal from "../components/Modal.vue";
import StatementImport from "../components/StatementImport.vue";

// TODO: save/read range to/from local storage

export default {
  name: "Statements",
  components: {StatementImport, Modal, Statement, FontAwesomeIcon, Breadcrumbs, Category, Card},
  data() {
    return {
      showImportModal: false,
      selectedStatement: null,
      autoSelectStatementTags: false,
      showInactive: false,
      dateFormatter: {
        date: 'YYYY-MM-DD',
        month: 'MMM'
      },
      dateShortcuts: () => [
        {
          label: 'This Month',
          atClick: () => [
            dayjs().startOf('month').format('YYYY-MM-DD'),
            dayjs().endOf('month').format('YYYY-MM-DD')
          ]
        },
        {
          label: '30 Days',
          atClick: () => [
            dayjs().subtract(30, 'days').format('YYYY-MM-DD'),
            dayjs().format('YYYY-MM-DD')
          ]
        },
        {
          label: '60 Days',
          atClick: () => [
            dayjs().subtract(60, 'days').startOf('month').format('YYYY-MM-DD'),
            dayjs().format('YYYY-MM-DD')
          ]
        },
        {
          label: '90 Days',
          atClick: () => [
            dayjs().subtract(90, 'days').startOf('month').format('YYYY-MM-DD'),
            dayjs().format('YYYY-MM-DD')
          ]
        },
        {
          label: '180 Days',
          atClick: () => [
            dayjs().subtract(180, 'days').startOf('month').format('YYYY-MM-DD'),
            dayjs().format('YYYY-MM-DD')
          ]
        },
        {
          label: 'Last Month',
          atClick: () => [
            dayjs().subtract(1, 'month').startOf('month').format('YYYY-MM-DD'),
            dayjs().subtract(1, 'month').endOf('month').format('YYYY-MM-DD')
          ]
        },
        {
          label: 'Last 3 Months',
          atClick: () => [
            dayjs().subtract(3, 'month').startOf('month').format('YYYY-MM-DD'),
            dayjs().subtract(1, 'month').endOf('month').format('YYYY-MM-DD')
          ]
        },
        {
          label: 'Last 6 Months',
          atClick: () => [
            dayjs().subtract(6, 'month').startOf('month').format('YYYY-MM-DD'),
            dayjs().subtract(1, 'month').endOf('month').format('YYYY-MM-DD')
          ]
        },
        {
          label: 'This Year',
          atClick: () => [
            dayjs().startOf('year').format('YYYY-MM-DD'),
            dayjs().endOf('year').format('YYYY-MM-DD')
          ]
        },
        {
          label: 'Last Year',
          atClick: () => [
            dayjs().subtract(1, 'year').startOf('year').format('YYYY-MM-DD'),
            dayjs().subtract(1, 'year').endOf('year').format('YYYY-MM-DD')
          ]
        }
      ],
      range: {
        from: null,
        to: null
      },
      categoryFilters: [],
      tagFilters: [],
      accountFilters: [],
      term: null,
      expandedTransfers: []
    }
  },
  watch: {
    range() {
      this.updateStatementFilters()
      this.refreshStatements()
    },
    term() {
      this.updateStatementFilters()
    }
  },
  computed: {
    accounts() {
      return this.$store.getters.getAccounts()
    },
    categories() {
      const categories = this.$store.getters.getCategories()
      categories.sort((a, b) => {
        const amountA = this.getCategorySum(a)
        const amountB = this.getCategorySum(b)
        return amountA - amountB
      })
      return categories
    },
    allStatements() {
      return this.$store.getters.getStatements(this.range.from, this.range.to)
    },
    statements() {
      return this.allStatements
          .filter(it => this.showInactive || it.attributes.state === 'ACTIVE')
          .filter(it => !this.statementLinks.some(link => link.relationships.firstStatement.data.id === it.id || link.relationships.secondStatement.data.id === it.id))
    },
    statementLinks() {
      return Object.values(this.$store.state.statementLinks);
    },
    entries() {
      let showLinks = this.tagFilters.length <= 0 && this.categoryFilters.length <= 0

      const links = showLinks && this.statements.length && this.statementLinks.map((it, index) => {
        const first = this.allStatements.find(statement => statement.id === it.relationships.firstStatement.data.id)
        const second = this.allStatements.find(statement => statement.id === it.relationships.secondStatement.data.id)

        const firstAccount = first && this.accounts.find(account => account.id === first.relationships.account.data.id)
        const secondAccount = second && this.accounts.find(account => account.id === second.relationships.account.data.id)

        return {
          type: 'transfer',
          id: Date.now() + '-' + index,
          date: second && second.attributes.valueDate,
          show: false,
          first, firstAccount,
          second, secondAccount
        }
      }).filter(it => {
        return it.first && it.second
      }).filter(it => {
        const containsFirstAccount = this.accountFilters.length <= 0 || this.accountFilters.includes(it.firstAccount.id)
        const containsSecondAccount = this.accountFilters.length <= 0 || this.accountFilters.includes(it.secondAccount.id)
        return containsFirstAccount || containsSecondAccount
      }) || []

      const statements = this.filteredStatements
          .map(it => {
            return {
              type: 'statement',
              id: it.id,
              date: it.attributes.valueDate,
              statement: it
            }
          })

      const entries = links.concat(statements)
      entries.sort((a, b) => {
        if (a.date === b.date) return b.id - a.id
        return a.date < b.date ? 1 : -1;
      })
      return entries
    },
    filteredStatements() {
      return this.statements
          .filter(it => this.isCategoryFilterActive(it.relationships.category.data.id))
          .filter(it => {
            const tagIds = it.relationships.tags.data.map(it => it.id)
            return this.tagFilters.length === 0 || tagIds.filter(it => this.isTagFilterActive(it)).length > 0
          })
          .filter(it => {
            const accountId = it.relationships.account.data.id
            return this.accountFilters.length === 0 || this.isAccountFilterActive(accountId)
          })
          .filter(it => {
            return !this.term || Object.values(it.attributes).some(it => it.toString().toLowerCase().includes(this.term.toLowerCase()))
          })
    },
    budgetFactor() {
      const from = dayjs(this.range.from)
      const to = dayjs(this.range.to)

      const months = to.diff(from, "months") === 0 ? 1 : Math.ceil(to.diff(from, "months", true) + 1)

      let diffMonths = 0
      for (let i = 0; i < months; i++) {
        const startOfMonth = from.add(i, "months").startOf("month")
        const endOfMonth = from.add(i, "months").endOf("month")

        const daysInMonth = endOfMonth.endOf("month").diff(startOfMonth.startOf("month"), "days", true)
        let actualDays = 0

        if (from.diff(to, "months") === 0) {
          actualDays = to.diff(from, "days", true) + 1
        } else if (from.isAfter(startOfMonth)) {
          actualDays = endOfMonth.diff(from, "days", true) + 1
        } else if (to.isBefore(endOfMonth)) {
          actualDays = to.diff(startOfMonth, "days", true) + 1
        } else {
          actualDays = daysInMonth
        }

        diffMonths += actualDays / daysInMonth
      }
      return diffMonths
    },
    total() {
      let sum = 0
      this.statements.forEach(it => sum += it.attributes.amount)
      return sum
    },
    filteredTotal() {
      let sum = 0
      this.filteredStatements.forEach(it => sum += it.attributes.amount)
      return sum
    },
    tags() {
      return this.$store.getters.getTags()
    }
  },
  beforeCreate() {
  },
  mounted() {
    this.$store.commit('initializeStatementFilters')
    this.range = this.$store.state.statementFilters.range
    this.categoryFilters = this.$store.state.statementFilters.categories
    this.tagFilters = this.$store.state.statementFilters.tags
    this.accountFilters = this.$store.state.statementFilters.accounts
    this.refreshEverything()
  },
  methods: {
    refreshEverything() {
      this.$store.dispatch('fetchAccounts')
      this.$store.dispatch('fetchCategories')
      this.$store.dispatch('fetchTags')
      this.$store.dispatch('fetchStatementLinks')
      this.refreshStatements()
    },
    refreshStatements() {
      this.$store.dispatch('fetchStatements', { from: this.range.from, to: this.range.to })
    },
    showModal(statement) {
      this.autoSelectStatementTags = false;

      if (!statement) {
        statement = {
          attributes: {
            bookingDate: dayjs().format('YYYY-MM-DD'),
            valueDate: dayjs().format('YYYY-MM-DD'),
            type: null,
            amount: 0,
            currency: '',
            purpose: null,
            creditorId: null,
            mandateReference: null,
            paymentInformationId: null,
            thirdPartyName: null,
            thirdPartyAccount: null,
            thirdPartyBankCode: null
          },
          relationships: {
            account: { data: { type: 'accounts',  id: this.accounts[0].id } },
            category: { data: { type: 'categories',  id: -1 } },
            tags: { data: [] }
          }
        }
      } else {
        statement = JSON.parse(JSON.stringify(statement))
      }
      this.selectedStatement = statement
    },
    hideModal() {
      this.selectedStatement = null
    },
    submitStatement() {
      if (this.selectedStatement == null) return;

      this.selectedStatement.relationships.tags.data = !this.autoSelectStatementTags
          ? Array.from(this.$refs.tagSelector.options).filter(it => it.selected).map(it => ({ type: 'tags', id: parseInt(it.value) }))
          : null

      const action = this.selectedStatement.id == null ? 'createStatement' : 'updateStatement'
      this.$store.dispatch(action, this.selectedStatement).then(() => {
        this.selectedStatement = null
      })
    },
    deleteStatement() {
      this.$store.dispatch('deleteStatement', { id: this.selectedStatement.id }).then(() => {
        this.selectedStatement = null
      })
    },
    isCategoryFilterActive(id) {
      return this.categoryFilters.length === 0 || this.categoryFilters.includes(id)
    },
    toggleCategoryFilter(id) {
      if (this.categoryFilters.includes(id)) {
        this.categoryFilters = this.categoryFilters.filter(it => it !== id)
      } else {
        this.categoryFilters = [...this.categoryFilters, id]
      }

      if (this.categoryFilters.length >= this.categories.length) {
        this.categoryFilters = []
      }

      this.updateStatementFilters()
    },
    isTagFilterActive(id) {
      return this.tagFilters.includes(id)
    },
    toggleTagFilter(id) {
      if (this.tagFilters.includes(id)) {
        this.tagFilters = this.tagFilters.filter(it => it !== id)
      } else {
        this.tagFilters = [...this.tagFilters, id]
      }

      this.updateStatementFilters()
    },
    isAccountFilterActive(id) {
      return this.accountFilters.includes(id)
    },
    toggleAccountFilter(id) {
      if (this.accountFilters.includes(id)) {
        this.accountFilters = this.accountFilters.filter(it => it !== id)
      } else {
        this.accountFilters = [...this.accountFilters, id]
      }

      this.updateStatementFilters()
    },
    updateStatementFilters() {
      this.$store.commit('setStatementFilters', {
        range: {
          from: this.range.from,
          to: this.range.to,
        },
        categories: this.categoryFilters,
        tags: this.tagFilters,
        accounts: this.accountFilters,
        term: this.term
      })
    },
    getCategoryStatements(category) {
      return this.statements
          .filter(it => it.relationships.category.data.id === category.id)
          .filter(it => it.attributes.state === 'ACTIVE')
    },
    getCategorySum(category) {
      let sum = 0
      const statements = this.getCategoryStatements(category)
      statements.forEach(it => sum += it.attributes.amount)
      return sum
    },
    getStatementTags(statement) {
      return statement.relationships.tags.data.map(it => it.id)
    },
    deleteStatements() {
      this.$store.dispatch('clearStatements')
    },
    applyPatterns() {
      this.$store.dispatch('applyPatterns').then(() => {
        this.refreshEverything()
      })
    },
    isTagActive(statement, id) {
      return statement.relationships.tags.data.map(it => it.id).includes(id)
    },
    clearTagFilters() {
      this.tagFilters = []
      this.updateStatementFilters()
    },
    clearAccountFilters() {
      this.accountFilters = []
      this.updateStatementFilters()
    },
    toggleTransfer(entry) {
      if (this.isExpanded(entry)) {
        this.expandedTransfers = this.expandedTransfers.filter(it => it !== entry.id)
      } else {
        this.expandedTransfers = [...this.expandedTransfers, entry.id]
      }
    },
    isExpanded(entry) {
      return this.expandedTransfers.includes(entry.id)
    }
  }
}
</script>
