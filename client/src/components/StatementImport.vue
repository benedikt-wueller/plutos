<template>
  <div class="fixed w-full h-full">
    <modal v-if="show" title="Import Transactions" @hide="this.$emit('hide')">
      <div>
        <div class="font-semibold">Importer</div>
        <select v-model="importer" class="rounded-md w-full">
          <option v-for="importer in importers" :value="importer.key">{{ importer.name }}</option>
        </select>
      </div>

      <div class="mt-2">
        <div class="font-semibold">File</div>
        <input type="file" :accept="fileType" multiple ref="import" class="w-full">
      </div>

      <div class="mt-2" v-for="entry in parameters" v-bind:key="'parameter-' + entry.key">
        <div class="font-semibold">{{ entry.name }}</div>
        <select v-if="entry.type === 'account'" class="rounded-md w-full" @change="event => setParameterValue(entry.key, event.target.value)">
          <option disabled selected>&mdash; Select an account &mdash;</option>
          <option v-for="account in accounts" v-bind:key="'parameter-account-' + account.id" :value="account.id">{{ account.attributes.name }}</option>
        </select>
      </div>

      <div class="mt-2 text-right">
        <button class="bg-blue-500 px-3 py-2 text-white rounded-md hover:bg-blue-600" @click="importTransactions">Upload</button>
      </div>
    </modal>
  </div>
</template>

<script>
import Modal from "./Modal.vue";
import Card from "./Card.vue";
import Breadcrumbs from "./Breadcrumbs.vue";

export default {
  name: "StatementImport",
  components: {Modal, Card, Breadcrumbs},
  props: {
    show: Boolean
  },
  data() {
    return {
      importer: null,
      parameterValues: {}
    }
  },
  watch: {
    importers() {
      this.importer = this.importers[0].key
    },
    selectedImporter() {
      if (this.selectedImporter == null) return
      const parameters = {}
      this.parameters.forEach(it => parameters[it.key] = null)
      this.parameterValues = parameters
    }
  },
  mounted() {
    this.$store.dispatch('fetchImporters')
  },
  computed: {
    importers() {
      return this.$store.state.importers
    },
    accounts() {
      return this.$store.getters.getAccounts()
    },
    selectedImporter() {
      return this.importers.find(it => it.key === this.importer)
    },
    fileType() {
      if (this.selectedImporter == null) return null
      return this.selectedImporter.fileFormat
    },
    parameters() {
      if (this.selectedImporter == null) return null
      return this.selectedImporter.parameters
    }
  },
  methods: {
    importTransactions() {
      const files = this.$refs.import.files
      if (!files || files.length < 1) return

      this.$store.dispatch('importStatements', {
        importer: this.importer,
        files: files,
        params: this.parameterValues
      }).then(() => {
        this.$emit('hide')
        this.$emit('imported')
      })
    },
    setParameterValue(key, value) {
      this.parameterValues = { ...this.parameterValues, [key]: value }
    }
  }
}
</script>

<style scoped>

</style>
