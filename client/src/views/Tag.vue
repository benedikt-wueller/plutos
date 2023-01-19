<template>
  <div v-if="tag">
    <breadcrumbs :breadcrumbs="[
        { name: 'Tags', 'path': '/tags' },
        { name: tag.attributes.name, 'path': '/tags/' + tag.id }
    ]"></breadcrumbs>

    <h1 class="mb-4 text-2xl font-semibold">{{ tag.attributes.name }} &ndash; Patterns</h1>

    <div v-if="!patterns || patterns.length === 0" class="mb-4 bg-blue-100 py-3 px-4 rounded-md">
      There are no patterns yet. This tag will not be assigned to new statements automatically.
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-4">
      <card v-for="pattern in patterns" v-bind:key="'pattern-' + pattern.id" class="hover:bg-gray-100 cursor-pointer" @click="showModal(pattern)">
        <div class="text-xl font-semibold flex-grow">{{ pattern.attributes.name }}</div>

        <div class="mt-2">
          <div class="font-semibold">Regex</div>
          <div class="font-mono rounded-md bg-gray-100 px-2 py-1">{{ pattern.attributes.regex }}</div>
        </div>

        <div class="mt-2">
          <div class="font-semibold">Match Mode</div>
          <div>{{ pattern.attributes.matchMode }}</div>
        </div>
      </card>
    </div>

    <h1 class="text-2xl font-semibold mb-4">Actions</h1>
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-4">
      <card class="bg-green-100 hover:bg-green-200 cursor-pointer" name="Add Pattern" icon="fa-solid fa-plus" @click="showModal(null)">
        <div class="mt-2">Add a new pattern to automatically assign this tag to newly imported transactions.</div>
      </card>
    </div>

    <PatternModal :selected-pattern="selectedPattern" @hide="hideModal" @delete="deletePattern" @submit="submitPattern"></PatternModal>
  </div>
</template>

<script>
import Breadcrumbs from "../components/Breadcrumbs.vue";
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import Card from "../components/Card.vue";
import Modal from "../components/Modal.vue";
import PatternModal from "../components/PatternModal.vue";

export default {
  name: "tag",
  components: {PatternModal, Modal, Card, FontAwesomeIcon, Breadcrumbs},
  data() {
    return {
      id: null,
      selectedPattern: null
    }
  },
  computed: {
    tag() {
      return this.$store.getters.findTag(this.id)
    },
    patterns() {
      return this.$store.getters.getTagPatterns(this.id)
    }
  },
  mounted() {
    this.id = this.$route.params['tagId']
    this.$store.dispatch('loadTag', { id: this.id })
    this.$store.dispatch('fetchTagPatterns', { id: this.id })
    this.$store.dispatch('fetchAccounts')
  },
  methods: {
    hideModal() {
      this.selectedPattern = null
    },
    showModal(pattern) {
      if (!pattern) {
        pattern = {
          attributes: {
            name: 'New Tag Pattern',
            regex: '.*',
            matchMode: 'PARTIAL_MATCH',
            matchTargets: [],
            accountTargets: [],
            squishData: false
          }
        }
      } else {
        pattern = JSON.parse(JSON.stringify(pattern))
      }
      this.selectedPattern = pattern
    },
    submitPattern() {
      if (this.selectedPattern == null) return;
      const action = this.selectedPattern.id == null ? 'createTagPattern' : 'updateTagPattern'
      this.$store.dispatch(action, {
        tagId: this.id,
        pattern : this.selectedPattern
      }).then(() => {
        this.selectedPattern = null
      })
    },
    deletePattern() {
      this.$store.dispatch('deleteTagPattern', { id: this.selectedPattern.id }).then(() => {
        this.selectedPattern = null
      })
    }
  }
}
</script>

<style scoped>

</style>
