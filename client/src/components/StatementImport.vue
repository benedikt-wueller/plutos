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
      importer: null
    }
  },
  watch: {
    importers() {
      this.importer = this.importers[0].key
    }
  },
  mounted() {
    this.$store.dispatch('fetchImporters')
  },
  computed: {
    importers() {
      return this.$store.state.importers
    },
    fileType() {
      const importer = this.importers.find(it => it.key === this.importer)
      if (importer == null) return null
      return importer.fileFormat
    }
  },
  methods: {
    importTransactions() {
      const files = this.$refs.import.files
      if (!files || files.length < 1) return

      this.$store.dispatch('importStatements', {
        importer: this.importer,
        files: files
      }).then(() => {
        this.$emit('hide')
        this.$emit('imported')
      })
    },
  }
}
</script>

<style scoped>

</style>
