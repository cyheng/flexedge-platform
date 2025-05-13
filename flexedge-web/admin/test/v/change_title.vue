<template>
    <div>
        <em>Change the title here:</em>
        <input :value="title" type="text" @input="onInput">
    </div>
</template>

<script>
export default {
    props: ['title'],
    methods: {
        onInput(event) {
            this.$emit('input', event.target.value);
        }
    }
}
</script>