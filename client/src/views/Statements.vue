<template>
  <div>
    <breadcrumbs :breadcrumbs="[{ name: 'Transactions', path: '/transactions' }]"></breadcrumbs>

    <div class="pb-8">
      <h1 class="text-2xl font-semibold mb-4">Range</h1>

      <vue-tailwind-datepicker :shortcuts="dateShortcuts" :formatter="dateFormatter" v-model="range" />

      <div class="p-4 mt-4 text-sm text-yellow-700 bg-yellow-100 rounded-lg" role="alert">
        Budget limits are calculated proportionally to the days/months selected.
      </div>
    </div>

    <div class="pb-8">
      <h1 class="text-2xl font-semibold mb-4">Categories & Budgets</h1>

      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-4">
        <a v-for="category in categories"
           v-bind:key="'category-' + category.id"
           @click="toggleFilter(category.id)"
           :class="{ 'opacity-50': !isFilterActive(category.id), 'cursor-pointer': true }">
          <category
              :id="category.id"
              :name="category.name"
              :budget="getBudget(category.id)"
              :amount="getTotal(category.id)"
              :currency="category.currency"></category>
        </a>
      </div>
    </div>

    <div class="pb-8">
      <h1 class="text-2xl font-semibold mb-4">Transactions</h1>

      <div class="grid grid-cols-1 gap-4">
        <Transaction v-for="transaction in filteredTransactions"
                 :transaction="transaction"
                 v-bind:key="'transaction-' + transaction.id" class="cursor-pointer"
                 @click="selectTransaction(transaction)"></Transaction>
      </div>
    </div>

    <transaction-modal v-if="modal.show"
                       :transaction="modal.transaction"
                       :categories="categories"
                       @hide="modal.show = false"
                       @save="saveTransaction"></transaction-modal>
  </div>
</template>

<script>
import Transaction from "../components/Transaction.vue";
import axios from "axios";
import dayjs from "dayjs";
import Category from "../components/Category.vue";
import Breadcrumbs from "../components/Breadcrumbs.vue";
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import TransactionModal from "../components/TransactionModal.vue";

export default {
  name: "Transactions",
  components: {TransactionModal, FontAwesomeIcon, Breadcrumbs, Category, Transaction},
  data() {
    return {
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
          label: 'Last Month',
          atClick: () => [
            dayjs().subtract(1, 'month').startOf('month').format('YYYY-MM-DD'),
            dayjs().subtract(1, 'month').endOf('month').format('YYYY-MM-DD')
          ]
        },
        {
          label: 'Last 30 Days',
          atClick: () => [
            dayjs().subtract(30, 'days').format('YYYY-MM-DD'),
            dayjs().subtract(1, 'days').format('YYYY-MM-DD')
          ]
        },
        {
          label: 'Last 2 Months',
          atClick: () => [
            dayjs().subtract(2, 'month').startOf('month').format('YYYY-MM-DD'),
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
      filters: [],
      modal: {
        show: false,
        transaction: null
      }
    }
  },
  watch: {
    range() {
      this.updateQueryParams()
      this.$store.dispatch('fetchTransactions', { from: this.range.from, to: this.range.to })
    }
  },
  computed: {
    transactions() {
      return this.$store.state.transactions
    },
    categories() {
      return this.$store.state.categories
    },
    filteredTransactions() {
      const activeCategories = this.categories.map(it => it.id).filter(this.isFilterActive)
      return this.transactions.filter(it => activeCategories.includes(it.category.id))
    }
  },
  mounted() {
    this.range.from = this.$route.query.from || dayjs().startOf('month').format('YYYY-MM-DD')
    this.range.to = this.$route.query.to || dayjs().endOf('month').format('YYYY-MM-DD')

    this.$store.dispatch('fetchTransactions', { from: this.range.from, to: this.range.to })
    this.$store.dispatch('fetchCategories').then(() => {
      const existingFilters = this.$route.query.filters
      if (existingFilters) {
        this.filters = existingFilters.split(",")
      } else {
        this.filters = this.categories.map(it => it.id)
      }
    })
  },
  methods: {
    toggleFilter(id) {
      if (this.filters.includes(id)) {
        this.filters = this.filters.filter(it => it !== id)
      } else {
        this.filters = [...this.filters, id]
      }
      this.updateQueryParams()
    },
    isFilterActive(id) {
      return this.filters.includes(id)
    },
    updateQueryParams() {
      this.$router.push({
        path: '/transactions',
        query: {
          from: this.range.from,
          to: this.range.to,
          filters: this.filters.length !== this.categories.length ? this.filters.join(",") : undefined
        }
      })
    },
    selectTransaction(transaction) {
      this.modal.transaction = transaction
      this.modal.show = true
    },
    saveTransaction(categoryId, comment) {
      console.log(categoryId, comment, this.categories)

      const transaction = this.modal.transaction
      const category = this.categories.find(it => it.id === categoryId)

      this.$store.dispatch('updateTransaction', { id: transaction.id, categoryId, comment }).then(() => {
        this.modal.show = false
        this.modal.transaction = null

        const otherTransaction = this.transactions.find(it => it.id === transaction.id)
        otherTransaction.category = category
        otherTransaction.comment = comment
      })
    },
    getTotal(categoryId) {
      let sum = 0
      this.transactions.filter(it => it.category.id == categoryId).forEach(it => sum += it.amount)
      return sum
    },
    getBudget(categoryId) {
      const category = this.categories.find(it => it.id == categoryId)
      if (category == null || category.budget == null) return null

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

      return category.budget * diffMonths
    }
  }
}
</script>

<style scoped>

</style>
