<template>
  <div>
    <breadcrumbs :breadcrumbs="[{ name: 'Accounts', path: '/accounts' }]"></breadcrumbs>

    <h1 class="text-2xl font-semibold mb-4">Accounts</h1>

    <div v-if="accounts.length === 0" class="mb-4 bg-blue-100 py-3 px-4 rounded-md">
      There are no accounts yet. Importing statements will automatically create related accounts.
    </div>

    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-4">
      <card v-for="account in accounts" :name="account.attributes.name" class="cursor-pointer hover:bg-gray-100" @click="showModal(account)">
        <div class="mt-2">
          <div class="font-semibold">Identifier</div>
          <div class="font-mono bg-gray-100 rounded-md py-1 px-2">{{ account.attributes.identifier }}</div>
        </div>
        <div class="mt-2 grid grid-cols-2 gap-2">
          <div>
            <div class="font-semibold">Currency</div>
            <div>{{ account.attributes.currency || '-' }}</div>
          </div>
          <div>
            <div class="font-semibold">Statements</div>
            <div>{{ account.relationships.statements.data.length }}</div>
          </div>
        </div>
        <div class="mt-2">
        </div>
      </card>
    </div>

    <h1 class="text-2xl font-semibold mb-4">Actions</h1>
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-4">
      <card name="Add Account" icon="fa-solid fa-plus" class="bg-green-100 hover:bg-green-200 cursor-pointer" @click="showModal(null)">
        <div class="mt-2">
          Create a new account.
        </div>
      </card>
    </div>

    <modal :title="selectedAccount.id ? 'Edit Account' : 'Add Account'" v-if="selectedAccount" @hide="hideModal">
      <div>
        <div class="font-semibold">Name</div>
        <input type="text" class="rounded-md w-full" placeholder="Name" v-model="selectedAccount.attributes.name" :class="{ 'border-red-500': !selectedAccount.attributes.name }">
      </div>

      <div class="mt-4">
        <div class="font-semibold">Identifier</div>
        <input type="text" class="rounded-md w-full" placeholder="Identifier" v-model="selectedAccount.attributes.identifier" :class="{ 'border-red-500': !selectedAccount.attributes.identifier }">
      </div>

      <div class="mt-4">
        <div class="font-semibold">Currency</div>
        <input type="text" min="3" max="3" class="rounded-md w-full" placeholder="Currency" v-model="selectedAccount.attributes.currency">
      </div>

      <div class="mt-4 pt-4 border-t-2 flex gap-2 flex">
        <input type="submit" value="Delete Account & Statements"
               v-if="selectedAccount.id"
               class="rounded-md bg-red-500 px-4 py-1.5 text-white cursor-pointer hover:bg-red-800"
               @click="deleteAccount">
        <div class="flex-grow"></div>
        <input type="submit" value="Save"
               class="rounded-md bg-blue-500 px-4 py-1.5 text-white cursor-pointer hover:bg-blue-800"
               @click="submitAccount">
      </div>
    </modal>
  </div>
</template>

<script>
import Breadcrumbs from "../components/Breadcrumbs.vue";
import Card from "../components/Card.vue";
import Modal from "../components/Modal.vue";

export default {
  name: "Accounts",
  components: {Modal, Card, Breadcrumbs},
  data() {
    return {
      selectedAccount: null
    }
  },
  computed: {
    accounts() {
      return this.$store.getters.getAccounts()
    }
  },
  mounted() {
    this.$store.dispatch('fetchAccounts')
  },
  methods: {
    hideModal() {
      this.selectedAccount = null
    },
    showModal(account) {
      if (!account) {
        account = {
          attributes: {
            name: 'New Account',
            currency: 'EUR'
          }
        }
      } else {
        account = JSON.parse(JSON.stringify(account))
      }
      this.selectedAccount = account
    },
    submitAccount() {
      if (this.selectedAccount == null) return;
      const action = this.selectedAccount.id == null ? 'createAccount' : 'updateAccount'
      this.$store.dispatch(action, this.selectedAccount).then(() => {
        this.selectedAccount = null
      })
    },
    deleteAccount() {
      this.$store.dispatch('deleteAccount', { id: this.selectedAccount.id }).then(() => {
        this.selectedAccount = null
      })
    }
  }
}
</script>

<style scoped>

</style>