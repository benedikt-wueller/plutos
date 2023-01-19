<template>
  <modal :title="selectedPattern.id ? 'Edit Pattern' : 'Add Pattern'" v-if="selectedPattern" @hide="hideModal">
    <div>
      <div class="font-semibold">Name</div>
      <input type="text" class="rounded-md w-full" placeholder="Name" v-model="selectedPattern.attributes.name">
    </div>

    <div class="mt-2">
      <div class="font-semibold">Regex</div>
      <input type="text" class="rounded-md w-full" placeholder="Name" v-model="selectedPattern.attributes.regex">
      <span class="text-gray-500">Embedded flag expressions are supported.</span>
    </div>

    <div class="mt-2">
      <div class="font-semibold">Match Mode</div>
      <select class="rounded-md w-full" @change="event => selectedPattern.attributes.matchMode = event.target.value">
        <option :selected="selectedPattern.attributes.matchMode === 'FULL_MATCH'" value="FULL_MATCH">Full Match</option>
        <option :selected="selectedPattern.attributes.matchMode === 'PARTIAL_MATCH'" value="PARTIAL_MATCH">Partial Match</option>
        <option :selected="selectedPattern.attributes.matchMode === 'NO_FULL_MATCH'" value="NO_FULL_MATCH">No Full Match</option>
        <option :selected="selectedPattern.attributes.matchMode === 'NO_PARTIAL_MATCH'" value="NO_PARTIAL_MATCH">No Partial Match</option>
      </select>
      <div class="text-gray-500">
        <span v-if="selectedPattern.attributes.matchMode === 'FULL_MATCH'">Applies this tag if any statement value matches the pattern exactly.</span>
        <span v-if="selectedPattern.attributes.matchMode === 'PARTIAL_MATCH'">Applies this tag if any statement value contains the pattern.</span>
        <span v-if="selectedPattern.attributes.matchMode === 'NO_FULL_MATCH'">Applies this tag <b>only if</b> no statement value matches the pattern exactly.</span>
        <span v-if="selectedPattern.attributes.matchMode === 'NO_PARTIAL_MATCH'">Applies this tag <b>only if</b> no statement value contains the pattern.</span>
      </div>
    </div>

    <div clasS="mt-2">
      <div class="font-semibold">Match Targets</div>
      <div class="form-check">
        <input v-model="matchAll"
               class="form-check-input appearance-none h-4 w-4 border border-gray-300 rounded-sm bg-white checked:bg-blue-600 checked:border-blue-600 focus:outline-none transition duration-200 mt-1 align-top bg-no-repeat bg-center bg-contain float-left mr-2 cursor-pointer"
               type="checkbox"
               id="flexCheckDefault">
        <label class="form-check-label inline-block text-gray-800" for="flexCheckDefault">
          Match all fields
        </label>
      </div>
      <select v-show="!matchAll" class="rounded-md w-full" multiple ref="matchTargets">
        <option v-for="target in matchTargets"
                v-bind:key="'match-target-' + target.key"
                :selected="selectedPattern.attributes.matchTargets.includes(target.key)"
                :value="target.key">{{ target.name }}</option>
      </select>
    </div>

    <div clasS="mt-2">
      <div class="font-semibold">Squish Data</div>
      <div class="form-check">
        <input v-model="selectedPattern.attributes.squishData"
               class="form-check-input appearance-none h-4 w-4 border border-gray-300 rounded-sm bg-white checked:bg-blue-600 checked:border-blue-600 focus:outline-none transition duration-200 mt-1 align-top bg-no-repeat bg-center bg-contain float-left mr-2 cursor-pointer"
               type="checkbox"
               id="flexCheckDefault1">
        <label class="form-check-label inline-block text-gray-800" for="flexCheckDefault1">
          Remove all whitespaces before applying pattern.
        </label>
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
</template>

<script>
import Modal from "./Modal.vue";

export default {
  name: "PatternModal",
  components: {Modal},
  props: {
    selectedPattern: Object
  },
  data() {
    return {
      matchAll: true,
      matchTargets: [
        {
          name: 'Purpose',
          key: 'PURPOSE'
        },
        {
          name: 'Creditor ID',
          key: 'CREDITOR_ID'
        },
        {
          name: 'Mandate Reference',
          key: 'MANDATE_REFERENCE'
        },
        {
          name: 'Customer Reference',
          key: 'CUSTOMER_REFERENCE'
        },
        {
          name: 'Payment Information ID',
          key: 'PAYMENT_INFORMATION_ID'
        },
        {
          name: 'Third Party Name',
          key: 'THIRD_PARTY_NAME'
        },
        {
          name: 'Third Party Account',
          key: 'THIRD_PARTY_ACCOUNT'
        },
        {
          name: 'Third Party Bank Code',
          key: 'THIRD_PARTY_BANK_CODE'
        }
      ]
    }
  },
  watch: {
    selectedPattern() {
      this.matchAll = this.selectedPattern && (!this.selectedPattern.attributes.matchTargets || this.selectedPattern.attributes.matchTargets.length <= 0)
    }
  },
  methods: {
    deletePattern() {
      this.$emit('delete')
    },
    submitPattern() {
      this.selectedPattern.attributes.matchTargets = !this.matchAll
          ? Array.from(this.$refs.matchTargets.options).filter(it => it.selected).map(it => it.value)
          : []
      this.$emit('submit')
    },
    hideModal() {
      this.$emit('hide')
    }
  }
}
</script>

<style scoped>

</style>
