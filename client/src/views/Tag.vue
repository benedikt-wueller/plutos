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

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 xl:grid-cols-5 gap-4 mb-4">
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
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 xl:grid-cols-5 gap-4 mb-4">
      <card class="bg-green-100 hover:bg-green-200 cursor-pointer" name="Add Pattern" icon="fa-solid fa-plus" @click="showModal(null)">
        <div class="mt-2">Add a new pattern to automatically assign this tag to newly imported transactions.</div>
      </card>
    </div>

    <modal :title="selectedPattern.id ? 'Edit Pattern' : 'Add Pattern'" v-if="selectedPattern" @hide="hideModal">
      <div>
        <div class="font-semibold">Name</div>
        <input type="text" class="rounded-md w-full" placeholder="Name" v-model="selectedPattern.attributes.name">
      </div>

      <div class="mt-4">
        <div class="font-semibold">Regex</div>
        <input type="text" class="rounded-md w-full" placeholder="Name" v-model="selectedPattern.attributes.regex">
      </div>

      <div class="mt-4">
        <div class="font-semibold">Match Mode</div>
        <div>
          <div class="form-check flex">
            <input class="form-check-input appearance-none rounded-full h-4 w-4 border border-gray-300 bg-white checked:bg-blue-600 checked:border-blue-600 focus:outline-none transition duration-200 mt-1 align-top bg-no-repeat bg-center bg-contain float-left mr-2 cursor-pointer"
                   type="radio"
                   name="flexRadioDefault"
                   id="flexRadioDefault1"
                   value="FULL_MATCH"
                   v-model="selectedPattern.attributes.matchMode">
            <label class="form-check-label inline-block text-gray-800" for="flexRadioDefault1">
              Full Match &mdash; applies this tag if any statement value matches the pattern exactly.
            </label>
          </div>
          <div class="form-check flex">
            <input class="form-check-input appearance-none rounded-full h-4 w-4 border border-gray-300 bg-white checked:bg-blue-600 checked:border-blue-600 focus:outline-none transition duration-200 mt-1 align-top bg-no-repeat bg-center bg-contain float-left mr-2 cursor-pointer"
                   type="radio"
                   name="flexRadioDefault"
                   id="flexRadioDefault2"
                   value="PARTIAL_MATCH"
                   v-model="selectedPattern.attributes.matchMode">
            <label class="form-check-label inline-block text-gray-800" for="flexRadioDefault2">
              Partial Match &mdash; applies this tag if any statement value contains the pattern.
            </label>
          </div>
          <div class="form-check flex">
            <input class="form-check-input appearance-none rounded-full h-4 w-4 border border-gray-300 bg-white checked:bg-blue-600 checked:border-blue-600 focus:outline-none transition duration-200 mt-1 align-top bg-no-repeat bg-center bg-contain float-left mr-2 cursor-pointer"
                   type="radio"
                   name="flexRadioDefault"
                   id="flexRadioDefault3"
                   value="NO_FULL_MATCH"
                   v-model="selectedPattern.attributes.matchMode">
            <label class="form-check-label inline-block text-gray-800" for="flexRadioDefault3">
              No Full Match &mdash; applies this tag <b>only if</b> no statement value matches the pattern exactly.
            </label>
          </div>
          <div class="form-check flex">
            <input class="form-check-input appearance-none rounded-full h-4 w-4 border border-gray-300 bg-white checked:bg-blue-600 checked:border-blue-600 focus:outline-none transition duration-200 mt-1 align-top bg-no-repeat bg-center bg-contain float-left mr-2 cursor-pointer"
                   type="radio"
                   name="flexRadioDefault"
                   id="flexRadioDefault4"
                   value="NO_PARTIAL_MATCH"
                   v-model="selectedPattern.attributes.matchMode">
            <label class="form-check-label inline-block text-gray-800" for="flexRadioDefault4">
              No Partial Match &mdash; applies this tag <b>only if</b> no statement value contains the pattern.
            </label>
          </div>
        </div>
      </div>

      <div class="mt-4 pt-4 border-t-2 flex gap-2 flex">
        <input type="submit" value="Delete"
               v-if="selectedPattern.id"
               class="rounded-md bg-red-500 px-4 py-1.5 text-white cursor-pointer hover:bg-red-800"
               @click="deletePattern">
        <div class="flex-grow"></div>
        <input type="submit" value="Save"
               class="rounded-md bg-blue-500 px-4 py-1.5 text-white cursor-pointer hover:bg-blue-800"
               @click="submitPattern">
      </div>
    </modal>
  </div>
</template>

<script>
import Breadcrumbs from "../components/Breadcrumbs.vue";
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import Card from "../components/Card.vue";
import Modal from "../components/Modal.vue";

export default {
  name: "tag",
  components: {Modal, Card, FontAwesomeIcon, Breadcrumbs},
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
            matchMode: 'FULL_MATCH',
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
