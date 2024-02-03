import mongoose from 'mongoose';

export type StreamDocument = mongoose.Document & {
    url: string;
    token: string;
};

const streamSchema = new mongoose.Schema<StreamDocument>(
    {
        url: { type: String, unique: true },
        token: String
    },
    { timestamps: true }
);

/**
 * Token encryption middleware.
 */
/* TODO: Implement this
streamSchema.pre("save", function save(next) {
    const user = this as StreamDocument;
    if (!user.isModified("password")) { return next(); }
    bcrypt.genSalt(10, (err, salt) => {
        if (err) { return next(err); }
        bcrypt.hash(user.password, salt, undefined, (err: mongoose.Error, hash) => {
            if (err) { return next(err); }
            user.password = hash;
            next();
        });
    });
});
*/

export const Stream = mongoose.model<StreamDocument>('Stream', streamSchema);
